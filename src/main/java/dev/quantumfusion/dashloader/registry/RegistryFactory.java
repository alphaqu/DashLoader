package dev.quantumfusion.dashloader.registry;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.api.DashObjectClass;
import dev.quantumfusion.dashloader.io.RegistrySerializer;
import dev.quantumfusion.dashloader.registry.data.ChunkData;
import dev.quantumfusion.dashloader.registry.data.ChunkFactory;
import dev.quantumfusion.dashloader.registry.data.StageData;
import dev.quantumfusion.dashloader.registry.factory.DashFactory;
import dev.quantumfusion.dashloader.registry.factory.MissingHandler;
import dev.quantumfusion.dashloader.util.RegistryUtil;
import dev.quantumfusion.hyphen.HyphenSerializer;
import dev.quantumfusion.hyphen.io.ByteBufferIO;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.*;
import java.util.function.Function;

public final class RegistryFactory {
	private final Object2IntOpenHashMap<?> dedup = new Object2IntOpenHashMap<>();
	private final Object2ByteMap<Class<?>> target2chunkMappings;
	private final List<MissingHandler<?>> missingHandlers;
	public final ChunkFactory<?, ?>[] chunks;

	public RegistryFactory() {
		this.target2chunkMappings = null;
		this.missingHandlers = null;
		this.chunks = null;
	}

	private RegistryFactory(ChunkFactory<?, ?>[] chunks, List<MissingHandler<?>> missingHandlers) {
		this.target2chunkMappings = new Object2ByteOpenHashMap<>();
		this.missingHandlers = missingHandlers;
		this.chunks = chunks;
	}

	public static <R, D extends Dashable<R>> RegistryFactory create(List<MissingHandler<?>> missingHandlers, List<DashObjectClass<?, ?>> dashObjects) {

		//noinspection unchecked
		ChunkFactory<R, D>[] chunks = new ChunkFactory[dashObjects.size()];
		RegistryFactory writer = new RegistryFactory(chunks, missingHandlers);

		if (dashObjects.size() > 63) {
			throw new RuntimeException("Hit group limit of 63. Please contact QuantumFusion if you hit this limit!");
		}

		for (int i = 0; i < dashObjects.size(); i++) {
			final DashObjectClass<R, D> dashObject = (DashObjectClass<R, D>) dashObjects.get(i);
			var factory = DashFactory.create(dashObject);
			var dashClass = dashObject.getDashClass();
			var name = dashClass.getSimpleName();
			chunks[i] = new ChunkFactory<>((byte) i, name, factory, dashObject);
			
			final DashObject declaredAnnotation = dashClass.getDeclaredAnnotation(DashObject.class);
			if (declaredAnnotation != null) {
				writer.target2chunkMappings.put(declaredAnnotation.value(), (byte) i);
			} else {
				throw new RuntimeException("No DashObject annotation for " + name);
			}
		}

		return writer;
	}

	@SuppressWarnings("unchecked")
	public <R> int add(R object) {
		if (this.dedup.containsKey(object)) {
			return this.dedup.getInt(object);
		}
		var targetClass = object.getClass();
		byte chunkPos = this.target2chunkMappings.getOrDefault(targetClass, (byte) -1);

		if (chunkPos == -1) {
			for (MissingHandler missingHandler : this.missingHandlers) {
				if (missingHandler.parentClass.isAssignableFrom(targetClass)) {
					Object output = missingHandler.func.apply(object, this);
					if (output != null) {
						return add(output);
					}
				}
			}
		}

		if (chunkPos == -1) {
			throw new RuntimeException("Could not find a ChunkWriter for " + targetClass);
		}

		var chunk = (ChunkFactory<R, ?>) this.chunks[chunkPos];
		final var objectPos = chunk.add(object, this);
		final int pointer = RegistryUtil.createId(objectPos, chunkPos);
		((Object2IntMap<R>) this.dedup).put(object, pointer);
		return pointer;
	}

	public <D> ChunkFactory.Entry<D> get(int id) {
		return (ChunkFactory.Entry<D>) this.chunks[RegistryUtil.getChunkId(id)].list.get(RegistryUtil.getObjectId(id));
	}

	public StageData[] export() {
		// Create a queue with the elements with no references
		var exposedQueue = new ArrayDeque<ChunkFactory.Entry<?>>();
		for (ChunkFactory<?, ?> chunk : chunks) {
			for (ChunkFactory.Entry<?> entry : chunk.list) {
				if (entry.references == 0) {
					entry.stage = 0;
					exposedQueue.offer(entry);
				}
			}
		}

		// This sets the correct stage for every element
		int stages = 1;
		// Go through the exposed nodes (ones without edges)
		while (!exposedQueue.isEmpty()) {
			// Remove the element from the exposed queue.
			var element = exposedQueue.poll();
			for (var dependencyId : element.dependencies) {
				// Make dependencies a stage above
				ChunkFactory.Entry<?> dependency = get(dependencyId);
				if (dependency.stage <= element.stage) {
					dependency.stage = element.stage + 1;
					if (dependency.stage >= stages) {
						stages = dependency.stage + 1;
					}
				}
				// Remove the edge, if the dependency no longer has references, add it to the queue.
				if (--dependency.references == 0) {
					exposedQueue.offer(dependency);
				}
			}
		}

		// Create the output
		StageData[] out = new StageData[stages];
		for (int i = 0; i < stages; i++) {
			ChunkData<?, ?>[] chunksOut = new ChunkData[this.chunks.length];

			for (int j = 0; j < this.chunks.length; j++) {
				ChunkFactory<?, ?> chunk = this.chunks[j];
				List<ChunkData.Entry<?>> dashablesOut = new ArrayList<>();
				for (int k = 0; k < chunk.list.size(); k++) {
					ChunkFactory.Entry<?> entry = chunk.list.get(k);
					if (entry.stage == i) {
						dashablesOut.add(new ChunkData.Entry<>(entry.data, k));
					}
				}

				chunksOut[j] = new ChunkData<>(chunk.chunkId, chunk.name, chunk.dashObject, dashablesOut.toArray(ChunkData.Entry[]::new));
			}

			out[stages - (i + 1)] = new StageData(chunksOut);
		}

		return out;
	}
}
