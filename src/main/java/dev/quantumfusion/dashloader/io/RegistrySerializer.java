package dev.quantumfusion.dashloader.io;

import com.github.luben.zstd.Zstd;
import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.api.DashObjectClass;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.io.fragment.Fragment;
import dev.quantumfusion.dashloader.io.fragment.SimplePiece;
import dev.quantumfusion.dashloader.io.fragment.SizePiece;
import dev.quantumfusion.dashloader.io.meta.*;
import dev.quantumfusion.dashloader.io.serializer.UnsafeByteBufferDef;
import dev.quantumfusion.dashloader.registry.RegistryFactory;
import dev.quantumfusion.dashloader.registry.data.ChunkData;
import dev.quantumfusion.dashloader.registry.data.ChunkFactory;
import dev.quantumfusion.dashloader.registry.data.StageData;
import dev.quantumfusion.dashloader.thread.ThreadHandler;
import dev.quantumfusion.dashloader.util.IOHelper;
import dev.quantumfusion.hyphen.HyphenSerializer;
import dev.quantumfusion.hyphen.SerializerFactory;
import dev.quantumfusion.hyphen.io.ByteBufferIO;
import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StepTask;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
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

	public <D extends Dashable<?>> HyphenSerializer<ByteBufferIO, D> getSerializer(DashObjectClass<?, D> dashObject) {
		return (HyphenSerializer<ByteBufferIO, D>) this.serializers.get(dashObject.getDashClass());
	}

	public CacheMetadata serialize(Path dir, RegistryFactory factory, Consumer<Task> taskConsumer) throws IOException {
		StageData[] stages = factory.export();

		SimplePiece[] value = new SimplePiece[stages.length];
		for (int i = 0; i < stages.length; i++) {
			StageData stage = stages[i];
			SimplePiece[] value2 = new SimplePiece[stage.chunks.length];
			for (int i1 = 0; i1 < stage.chunks.length; i1++) {
				ChunkData<?, ?> chunk = stage.chunks[i1];
				HyphenSerializer serializer = getSerializer(chunk.dashObject);
				SizePiece[] value3 = new SizePiece[chunk.dashables.length];
				for (int i2 = 0; i2 < chunk.dashables.length; i2++) {
					value3[i2] = new SizePiece(serializer.measure(chunk.dashables[i2].data) + 4);
				}

				value2[i1] = new SimplePiece(value3);
			}

			value[i] = new SimplePiece(value2);
		}
		SimplePiece piece = new SimplePiece(value);

		int[][] stageSizes = new int[stages.length][];
		for (int i = 0; i < stages.length; i++) {
			StageData stage = stages[i];
			int[] chunkSizes = new int[stage.chunks.length];
			for (int i1 = 0; i1 < stage.chunks.length; i1++) {
				chunkSizes[i1] = stage.chunks[i1].dashables.length;
			}
			stageSizes[i] = chunkSizes;
		}



		// Calculate amount of fragments required
		int minFragments = (int) (piece.size / MAX_FRAGMENT_SIZE);
		int maxFragments = (int) (piece.size / MIN_PER_THREAD_FRAGMENT_SIZE);
		int fragmentCount = Integer.max(Integer.max(Integer.min(ThreadHandler.THREADS, maxFragments), minFragments), 1);
		long remainingSize = piece.size;

		List<FragmentMetadata> fragments = new ArrayList<>();
		for (int i = 0; i < fragmentCount; i++) {
			long fragmentSize = remainingSize / (fragmentCount - i);
			if (i == fragmentCount - 1) {
				fragmentSize = Long.MAX_VALUE;
			}
			Fragment fragment = piece.fragment(fragmentSize);
			remainingSize -= fragment.size;
			fragments.add(new FragmentMetadata(fragment));
		}


		StepTask task = new StepTask("fragment", fragments.size() * 2);
		taskConsumer.accept(task);
		// Serialize
		for (int k = 0; k < fragments.size(); k++) {
			DashLoader.LOG.info("Serializing fragment " + k);
			FragmentMetadata fragment = fragments.get(k);
			List<StageFragmentMetadata> stageFragmentMetadata = fragment.stages;
			ByteBufferIO io = ByteBufferIO.createDirect((int) fragment.info.fileSize);


			int taskSize = 0;
			for (var stage : stageFragmentMetadata) {
				for (var chunk : stage.chunks) {
					taskSize += chunk.info.rangeEnd - chunk.info.rangeStart;
				}
			}

			StepTask stageTask = new StepTask("stage", taskSize);
			task.setSubTask(stageTask);
			for (int i = 0; i < stageFragmentMetadata.size(); i++) {
				StageFragmentMetadata stage = stageFragmentMetadata.get(i);
				StageData data = stages[i + fragment.info.rangeStart];

				List<ChunkFragmentMetadata> chunks = stage.chunks;
				for (int j = 0; j < chunks.size(); j++) {
					ChunkFragmentMetadata chunk = chunks.get(j);
					ChunkData<?, ?> chunkData = data.chunks[j + stage.info.rangeStart];
					HyphenSerializer serializer = serializers.get(chunkData.dashObject.getDashClass());
					for (int i1 = chunk.info.rangeStart; i1 < chunk.info.rangeEnd; i1++) {
						ChunkData.Entry<?> dashable = chunkData.dashables[i1];
						io.putInt(dashable.pos);
						serializer.put(io, dashable.data);
						stageTask.next();
					}
				}
			}
			task.next();

			StepTask serializingTask = new StepTask("Serializing");
			task.setSubTask(serializingTask);

			int fileSize = (int) fragment.info.fileSize;
			try (FileChannel channel = IOHelper.createFile(fragmentFilePath(dir, k))) {
				if (compressionLevel > 0) {
					serializingTask.reset(3);

					// Compress
					io.rewind();
					final long maxSize = Zstd.compressBound(fileSize);
					final var dst = ByteBufferIO.createDirect((int) maxSize);
					serializingTask.next();

					final long size = Zstd.compress(dst.byteBuffer, io.byteBuffer, compressionLevel);
					io.close();
					serializingTask.next();

					// Write
					dst.rewind();
					dst.byteBuffer.limit((int) size);
					final var map = channel.map(FileChannel.MapMode.READ_WRITE, 0, size).order(ByteOrder.LITTLE_ENDIAN);
					map.put(dst.byteBuffer);
					map.clear();
					io.close();
					dst.close();
					serializingTask.next();
				} else {
					serializingTask.reset(1);
					final var map = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize).order(ByteOrder.LITTLE_ENDIAN);
					io.byteBuffer.rewind();
					map.put(0, io.byteBuffer, 0, fileSize);
					serializingTask.next();
				}
			}
			task.next();
		}

		List<ChunkInfo> chunks = new ArrayList<>();
		for (ChunkFactory<?, ?> chunk : factory.chunks) {
			chunks.add(new ChunkInfo(chunk));
		}

		return new CacheMetadata(fragments, stageSizes, chunks, System.currentTimeMillis(), -1, 0, compressionLevel);
	}

	public StageData[] deserialize(Path dir, CacheMetadata metadata, List<DashObjectClass<?, ?>> objects) throws IOException {
		StageData[] out = new StageData[metadata.stageSizes.length];
		for (int i = 0; i < metadata.stageSizes.length; i++) {
			int[] chunkSizes = metadata.stageSizes[i];
			ChunkData[] chunks = new ChunkData[chunkSizes.length];
			for (int j = 0; j < chunks.length; j++) {
				ChunkInfo chunkInfo = metadata.chunks.get(j);
				chunks[j] = new ChunkData(
						(byte) j,
						chunkInfo.name,
						objects.get(chunkInfo.dashObjectId),
						new ChunkData.Entry[chunkSizes[j]]
				);
			}

			out[i] = new StageData(chunks);
		}


		List<FragmentMetadata> fragments = metadata.fragments;
		List<Runnable> runnables = new ArrayList<>();
		for (int j = 0; j < fragments.size(); j++) {
			FragmentMetadata fragment = fragments.get(j);

			try (FileChannel channel = IOHelper.openFile(fragmentFilePath(dir, j))) {
				var buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()).order(ByteOrder.LITTLE_ENDIAN);
				// Check compression
				runnables.add(() -> {
					if (compressionLevel > 0) {
						final var dst = ByteBufferIO.createDirect((int) fragment.info.fileSize);
						Zstd.decompress(dst.byteBuffer, buffer);
						dst.rewind();

						deserialize(out, dst, fragment);
						dst.close();
					} else {
						deserialize(out, ByteBufferIO.wrap(buffer), fragment);
					}
				});
			}
		}
		DashLoader.DL.thread.parallelRunnable(runnables);

		return out;
	}


	private void deserialize(StageData[] data, ByteBufferIO io, FragmentMetadata fragment) {
		for (int i = 0; i < fragment.stages.size(); i++) {
			StageFragmentMetadata stageFragment = fragment.stages.get(i);
			StageData stage = data[fragment.info.rangeStart + i];
			for (int i1 = 0; i1 < stageFragment.chunks.size(); i1++) {
				ChunkFragmentMetadata chunkFragment = stageFragment.chunks.get(i1);
				ChunkData chunkData = stage.chunks[stageFragment.info.rangeStart + i1];
				HyphenSerializer serializer = getSerializer(chunkData.dashObject);
				for (int i2 = chunkFragment.info.rangeStart; i2 < chunkFragment.info.rangeEnd; i2++) {
					int pos = io.getInt();
					Object out = serializer.get(io);
					chunkData.dashables[i2] = new ChunkData.Entry<>(out, pos);
				}
			}
		}
	}

	private Path fragmentFilePath(Path dir, int fragment) {
		return dir.resolve("fragment-" + fragment + ".bin");
	}
}
