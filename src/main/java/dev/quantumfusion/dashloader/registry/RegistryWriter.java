package dev.quantumfusion.dashloader.registry;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.registry.chunk.WriteChunk;
import dev.quantumfusion.dashloader.registry.factory.MissingHandler;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.ArrayList;
import java.util.List;

public final class RegistryWriter {
	private final Object2IntOpenHashMap<?> dedup = new Object2IntOpenHashMap<>();
	private final Object2ByteMap<Class<?>> target2chunkMappings;
	private final Object2ByteMap<Class<?>> dash2chunkMappings;
	private final List<MissingHandler<?>> missingHandlers;
	public final WriteChunk<?, ?>[] chunks;

	public RegistryWriter(WriteChunk<?, ?>[] chunks, List<MissingHandler<?>> missingHandlers) {
		this.target2chunkMappings = new Object2ByteOpenHashMap<>();
		this.dash2chunkMappings = new Object2ByteOpenHashMap<>();
		this.missingHandlers = missingHandlers;
		this.chunks = chunks;
	}

	public static int createPointer(int objectPos, byte chunkPos) {
		if (chunkPos > 0b111111) {
			throw new IllegalStateException("Chunk pos is too big. " + chunkPos + " > " + 0x3f);
		}
		if (objectPos > 0x3ffffff) {
			throw new IllegalStateException("Object pos is too big. " + objectPos + " > " + 0x3ffffff);
		}
		return objectPos << 6 | (chunkPos & 0x3f);
	}

	void addChunkMapping(Class<?> dashClass, byte pos) {
		this.dash2chunkMappings.put(dashClass, pos);
		final DashObject declaredAnnotation = dashClass.getDeclaredAnnotation(DashObject.class);
		if (declaredAnnotation != null) {
			this.target2chunkMappings.put(declaredAnnotation.value(), pos);
		} else {
			throw new RuntimeException("No DashObject annotation for " + dashClass.getSimpleName());
		}
	}

	@SuppressWarnings("unchecked")
	public <R, D extends Dashable<R>> WriteChunk<R, D> getChunk(Class<D> dashType) {
		return (WriteChunk<R, D>) this.chunks[this.dash2chunkMappings.getByte(dashType)];
	}

	public <R, D extends Dashable<R>> int addDirect(WriteChunk<R, D> chunk, R object) {
		final int objectPos = chunk.add(object);
		final int pointer = createPointer(objectPos, chunk.chunkId);
		//noinspection unchecked
		((Object2IntMap<R>) this.dedup).put(object, pointer);
		return pointer;
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

		var chunk = (WriteChunk<R, ?>) this.chunks[chunkPos];
		final var objectPos = chunk.add(object);
		final int pointer = createPointer(objectPos, chunkPos);
		((Object2IntMap<R>) this.dedup).put(object, pointer);
		return pointer;
	}
}
