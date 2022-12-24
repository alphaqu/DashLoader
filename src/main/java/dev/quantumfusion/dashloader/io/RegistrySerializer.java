package dev.quantumfusion.dashloader.io;

import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.api.DashObjectClass;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.io.meta.CacheMetadata;
import dev.quantumfusion.dashloader.io.meta.ChunkMetadata;
import dev.quantumfusion.dashloader.io.meta.FragmentMetadata;
import dev.quantumfusion.dashloader.io.serializer.UnsafeByteBufferDef;
import dev.quantumfusion.dashloader.registry.chunk.DataChunk;
import dev.quantumfusion.dashloader.registry.chunk.WriteChunk;
import dev.quantumfusion.dashloader.thread.ThreadHandler;
import dev.quantumfusion.hyphen.HyphenSerializer;
import dev.quantumfusion.hyphen.SerializerFactory;
import dev.quantumfusion.hyphen.io.ByteBufferIO;
import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StepTask;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RegistrySerializer {
	private static final int FRAGMENT_HEADER_SIZE = 4;
	// 20MB
	private static final int MIN_PER_THREAD_FRAGMENT_SIZE = 1024 * 1024 * 20;
	// 1GB
	private static final int MAX_FRAGMENT_SIZE = 1024 * 1024 * 1024;
	private Object2ObjectMap<Class<?>, HyphenSerializer<ByteBufferIO, ?>> serializers = null;
	private int compressionLevel;

	public RegistrySerializer() {
	}

	public void init(List<DashObjectClass<?, ?>> dashObjects, int compressionLevel) {
		this.compressionLevel = compressionLevel;
		this.serializers = new Object2ObjectOpenHashMap<>();
		for (DashObjectClass<?, ?> dashObject : dashObjects) {
			Class<?> dashClass = dashObject.getDashClass();

			SerializerFactory<ByteBufferIO, ?> factory = SerializerFactory.createDebug(ByteBufferIO.class, dashClass);
			factory.addDynamicDef(ByteBuffer.class, UnsafeByteBufferDef::new);
			this.serializers.put(dashClass, factory.build());
		}
	}

	public CacheMetadata serialize(Path dir, WriteChunk<?, ?>[] chunks, Consumer<Task> taskConsumer) throws IOException {
		List<Chunk> out = new ArrayList<>();
		long fileSize = 0;
		// Calculate all the fragments
		for (var chunk : chunks) {
			var dashClass = chunk.dashObject;
			var data = (DataChunk<?, ?>) chunk.exportData();
			HyphenSerializer serializer = serializers.get(dashClass.getDashClass());

			// Calculate sizes of the elements
			long chunkSize = 0;
			long[] elementSizes = new long[data.dashables.length];
			Dashable<?>[] dashables = data.dashables;
			for (int i = 0; i < dashables.length; i++) {
				long elementSize = serializer.measure(dashables[i]);
				chunkSize += elementSize;
				elementSizes[i] = elementSize;
			}
			fileSize += chunkSize;
			// Calculate amount of fragments required
			int minFragments = (int) (chunkSize / MAX_FRAGMENT_SIZE);
			int maxFragments = (int) (chunkSize / MIN_PER_THREAD_FRAGMENT_SIZE);
			int fragmentCount = Integer.max(Integer.max(Integer.min(ThreadHandler.THREADS, maxFragments), minFragments), 1);
			int averageFragmentSize = (int) (chunkSize / (long) fragmentCount);

			// Add elements to fragments
			int elementPos = 0;
			var fragments = new ArrayList<FragmentMetadata>();
			for (int i = 0; i < fragmentCount; i++) {
				int rangeStart = elementPos;
				int currentSize = 0;
				// Add until we reach the intended size, or we hit the last element.
				while ((currentSize < averageFragmentSize || i == fragmentCount - 1) && elementPos < elementSizes.length) {
					currentSize += elementSizes[elementPos++];
				}
				int rangeEnd = elementPos;

				fragments.add(new FragmentMetadata(rangeStart, rangeEnd, currentSize));
			}
			out.add(new Chunk(new ChunkMetadata(chunk.chunkId, chunk.name, chunk.dashObject.getDashObjectId(), data.dashables.length, fileSize, fragments), dashables, serializer));
		}

		StepTask chunkTask = new StepTask("Chunk", chunks.length);
		taskConsumer.accept(chunkTask);

		// TODO parallelize?
		// Serialize the fragments
		for (int i = 0; i < out.size(); i++) {
			Chunk<?> chunk = out.get(i);
			DashLoader.LOG.info("Serializing {}", chunk.metadata.name);

			List<FragmentMetadata> fragments = chunk.metadata.fragments;
			StepTask fragmentTask = new StepTask("Fragment", fragments.size());
			chunkTask.setSubTask(fragmentTask);
			for (int j = 0; j < fragments.size(); j++) {

				FragmentMetadata fragment = fragments.get(j);
				long l = Runtime.getRuntime().freeMemory();
				DashLoader.LOG.info("Serializing Fragment {} ({}MB) with {}MB", j, fragment.fileSize / 1024 / 1024, l / 1024 / 1024);
				fragment.serialize(chunk.dashables, chunk.serializer, fragmentFilePath(dir, i, j), compressionLevel, fragmentTask);
				fragmentTask.next();
			}
			chunkTask.next();
		}

		ChunkMetadata[] chunksOut = new ChunkMetadata[chunks.length];
		for (Chunk chunk : out) {
			chunksOut[chunk.metadata.chunkId] = chunk.metadata;
		}
		return new CacheMetadata(chunksOut, System.currentTimeMillis(), -1, 0, compressionLevel);
	}

	public DataChunk<?, ?>[] deserialize(Path dir, CacheMetadata metadata, List<DashObjectClass<?, ?>> objects) throws IOException {
		ChunkMetadata[] chunks = metadata.chunks;
		DataChunk<?, ?>[] dataChunks = new DataChunk[chunks.length];
		for (int i = 0; i < chunks.length; i++) {
			ChunkMetadata chunk = chunks[i];
			DashObjectClass<?, ?> dashObjectClass = objects.get(chunk.dashObjectId);
			HyphenSerializer<ByteBufferIO, ?> serializer = serializers.get(dashObjectClass.getDashClass());
			Dashable[] output = new Dashable[chunk.size];
			DashLoader.LOG.info("Deserializing " + chunk.name + "(size: " + chunk.size + ")");

			// TODO parallelize
			List<FragmentMetadata> fragments = chunk.fragments;

			List<Runnable> runnables = new ArrayList<>();
			for (int j = 0; j < fragments.size(); j++) {
				FragmentMetadata fragment = fragments.get(j);
				int finalI = i;
				int finalJ = j;

				try {
					runnables.add(fragment.deserialize(output, serializer, fragmentFilePath(dir, finalI, finalJ), metadata.compressionLevel));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			long l = Runtime.getRuntime().freeMemory();
			if (chunk.fileSize > (l / 2)) {
				DashLoader.LOG.warn("Running deserialization of " + chunk.name + " on a single thread because of limited memory.");
				for (Runnable runnable : runnables) {
					runnable.run();
				}
			} else {
				DashLoader.DL.thread.parallelRunnable(runnables);
			}

			//for (Runnable runnable : runnables) {
			//	runnable.run();
			//}
			DashLoader.LOG.info("");
			dataChunks[i] = new DataChunk<>(chunk.chunkId, chunk.name, output);
		}

		return dataChunks;
	}

	private Path fragmentFilePath(Path dir, int chunk, int fragment) {
		return dir.resolve("fragment-" + chunk + "-" + fragment + ".bin");
	}


	public static final class Chunk<V extends Dashable<?>> {
		public final ChunkMetadata metadata;
		public final V[] dashables;
		public final HyphenSerializer<ByteBufferIO, V> serializer;

		public Chunk(ChunkMetadata metadata, V[] dashables, HyphenSerializer<ByteBufferIO, V> serializer) {
			this.metadata = metadata;
			this.dashables = dashables;
			this.serializer = serializer;
		}
	}

}
