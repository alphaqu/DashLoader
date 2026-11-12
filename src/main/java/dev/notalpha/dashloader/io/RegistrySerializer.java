package dev.notalpha.dashloader.io;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.DashObjectClass;
import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.io.data.CacheInfo;
import dev.notalpha.dashloader.misc.ProfilerUtil;
import dev.notalpha.dashloader.io.data.ChunkInfo;
import dev.notalpha.dashloader.io.data.fragment.CacheFragment;
import dev.notalpha.dashloader.io.data.fragment.ChunkFragment;
import dev.notalpha.dashloader.io.data.fragment.StageFragment;
import dev.notalpha.dashloader.io.fragment.Fragment;
import dev.notalpha.dashloader.io.fragment.SimplePiece;
import dev.notalpha.dashloader.io.fragment.SizePiece;
import dev.notalpha.dashloader.registry.RegistryWriterImpl;
import dev.notalpha.dashloader.registry.data.ChunkData;
import dev.notalpha.dashloader.registry.data.ChunkFactory;
import dev.notalpha.dashloader.registry.data.StageData;
import dev.notalpha.dashloader.thread.ThreadHandler;
import dev.notalpha.hyphen.io.ByteBufferIO;
import dev.notalpha.taski.Task;
import dev.notalpha.taski.builtin.StepTask;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RegistrySerializer {
	// 20MB
	private static final int MIN_PER_THREAD_FRAGMENT_SIZE = 1024 * 1024 * 20;
	private final Object2ObjectMap<Class<?>, Serializer<?>> serializers;

	public RegistrySerializer(List<DashObjectClass<?, ?>> dashObjects) {
		this.serializers = new Object2ObjectOpenHashMap<>();
		for (DashObjectClass<?, ?> dashObject : dashObjects) {
			Class<?> dashClass = dashObject.getDashClass();
			this.serializers.put(dashClass, new Serializer<>(dashClass));
		}
	}

	public <D extends DashObject<?, ?>> Serializer<D> getSerializer(DashObjectClass<?, D> dashObject) {
		return (Serializer<D>) this.serializers.get(dashObject.getDashClass());
	}

	public CacheInfo serialize(Path dir, RegistryWriterImpl factory, Consumer<Task> taskConsumer) throws IOException {
		StageData[] stages = factory.export();

		long measureStart = System.currentTimeMillis();

		SimplePiece[] value = new SimplePiece[stages.length];
		List<Runnable> measureTasks = new ArrayList<>(stages.length);
		for (int i = 0; i < stages.length; i++) {
			final int index = i;
			measureTasks.add(() -> {
				StageData stage = stages[index];
				SimplePiece[] value2 = new SimplePiece[stage.chunks.length];
				for (int i1 = 0; i1 < stage.chunks.length; i1++) {
					ChunkData<?, ?> chunk = stage.chunks[i1];
					Serializer serializer = getSerializer(chunk.dashObject);
					SizePiece[] value3 = new SizePiece[chunk.dashables.length];
					for (int i2 = 0; i2 < chunk.dashables.length; i2++) {
						value3[i2] = new SizePiece(serializer.measure(chunk.dashables[i2].data) + 4);
					}

					value2[i1] = new SimplePiece(value3);
				}

				value[index] = new SimplePiece(value2);
			});
		}
		ThreadHandler.INSTANCE.parallelRunnable(measureTasks);
		ProfilerUtil.phase("measuring", System.currentTimeMillis() - measureStart);
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

		// Calculate amount of fragments required.
		// One fragment per MIN_PER_THREAD_FRAGMENT_SIZE per thread, so that both
		// writing and reading can actually use every thread. A former "one
		// fragment per gigabyte" rule forced small caches into a single fragment,
		// which serialised and loaded them on one thread only.
		int maxFragments = (int) Math.min(piece.size / MIN_PER_THREAD_FRAGMENT_SIZE, Integer.MAX_VALUE);
		int fragmentCount = Integer.max(Math.min(ThreadHandler.THREADS, maxFragments), 1);
		long remainingSize = piece.size;

		List<CacheFragment> fragments = new ArrayList<>();
		for (int i = 0; i < fragmentCount; i++) {
			long fragmentSize = remainingSize / (fragmentCount - i);
			if (i == fragmentCount - 1) {
				fragmentSize = Long.MAX_VALUE;
			}
			Fragment fragment = piece.fragment(fragmentSize);
			remainingSize -= fragment.size;
			fragments.add(new CacheFragment(fragment));
		}

		StepTask task = new StepTask("fragment", fragments.size());
		taskConsumer.accept(task);
		long writeStart = System.currentTimeMillis();
		List<Callable<Void>> fragmentTasks = new ArrayList<>(fragments.size());
		for (int k = 0; k < fragments.size(); k++) {
			final int index = k;
			fragmentTasks.add(() -> {
				writeFragment(dir, index, fragments.get(index), stages, serializers);
				return null;
			});
		}
		ThreadHandler.INSTANCE.forEachCompleted(fragmentTasks, task::next);
		ProfilerUtil.phase("writing fragments", System.currentTimeMillis() - writeStart);

		List<ChunkInfo> chunks = new ArrayList<>();
		for (ChunkFactory<?, ?> chunk : factory.chunks) {
			chunks.add(new ChunkInfo(chunk));
		}

		return new CacheInfo(fragments, chunks, stageSizes);
	}

	private static final ThreadLocal<ByteBuffer> SCRATCH = ThreadLocal.withInitial(() -> MemoryUtil.memAlloc(1));

	private static void writeFragment(Path dir, int index, CacheFragment fragment, StageData[] stages,
			java.util.Map<Class<?>, Serializer<?>> serializers) {
		DashLoader.LOG.info("Serializing fragment {}", index);
		List<StageFragment> stageFragmentMetadata = fragment.stages;
		ByteBuffer buffer = SCRATCH.get();
		int needed = (int) fragment.info.fileSize;
		if (buffer.capacity() < needed) {
			if (buffer.capacity() > 1) {
				MemoryUtil.memFree(buffer);
			}
			buffer = MemoryUtil.memAlloc(needed);
			SCRATCH.set(buffer);
		}
		try {
			buffer.clear();
			ByteBufferIO io = ByteBufferIO.wrap(buffer);

			for (int i = 0; i < stageFragmentMetadata.size(); i++) {
				StageFragment stage = stageFragmentMetadata.get(i);
				StageData data = stages[i + fragment.info.rangeStart];

				List<ChunkFragment> chunks = stage.chunks;
				for (int j = 0; j < chunks.size(); j++) {
					ChunkFragment chunk = chunks.get(j);
					ChunkData<?, ?> chunkData = data.chunks[j + stage.info.rangeStart];
					Serializer serializer = serializers.get(chunkData.dashObject.getDashClass());
					for (int i1 = chunk.info.rangeStart; i1 < chunk.info.rangeEnd; i1++) {
						ChunkData.Entry<?> dashable = chunkData.dashables[i1];
						io.putInt(dashable.pos);
						serializer.put(io, dashable.data);
					}
				}
			}

			IOHelper.save(dir.resolve("fragment-" + index + ".bin"), new StepTask("Serializing"), io,
					(int) fragment.info.fileSize, ConfigHandler.INSTANCE.config.compression);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			buffer.position(0);
		}
	}

	public StageData[] deserialize(Path dir, CacheInfo metadata, List<DashObjectClass<?, ?>> objects) {
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

		List<CacheFragment> fragments = metadata.fragments;
		List<Runnable> runnables = new ArrayList<>();
		for (int j = 0; j < fragments.size(); j++) {
			CacheFragment fragment = fragments.get(j);
			int finalJ = j;
			runnables.add(() -> {
				ByteBufferIO io;
				try {
					io = IOHelper.load(fragmentFilePath(dir, finalJ));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				try {
					deserialize(out, io, fragment);
				} finally {
					IOHelper.release(io);
				}
			});
		}

		if (ConfigHandler.INSTANCE.config.singleThreadedReading) {
			for (Runnable runnable : runnables) {
				runnable.run();
			}
		} else {
			ThreadHandler.INSTANCE.parallelRunnable(runnables);
		}

		return out;
	}

	private void deserialize(StageData[] data, ByteBufferIO io, CacheFragment fragment) {
		for (int i = 0; i < fragment.stages.size(); i++) {
			StageFragment stageFragment = fragment.stages.get(i);
			StageData stage = data[fragment.info.rangeStart + i];
			for (int i1 = 0; i1 < stageFragment.chunks.size(); i1++) {
				ChunkFragment chunkFragment = stageFragment.chunks.get(i1);
				ChunkData chunkData = stage.chunks[stageFragment.info.rangeStart + i1];
				Serializer serializer = getSerializer(chunkData.dashObject);
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