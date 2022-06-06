package dev.quantumfusion.dashloader.registry;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.registry.chunk.write.AbstractWriteChunk;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public final class RegistryWriter {
	private final Object2IntOpenHashMap<?> dedup = new Object2IntOpenHashMap<>();
	private final Object2ByteMap<Class<?>> target2chunkMappings;
	private final Object2ByteMap<Class<?>> dashTag2chunkMappings;
	private final Object2ByteMap<Class<?>> mappings;
	private final AbstractWriteChunk<?, ?>[] chunks;

	public RegistryWriter(AbstractWriteChunk<?, ?>[] chunks) {
		this.target2chunkMappings = new Object2ByteOpenHashMap<>();
		this.dashTag2chunkMappings = new Object2ByteOpenHashMap<>();
		this.mappings = new Object2ByteOpenHashMap<>();
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

	void compileMappings() {
		for (int i = 0; i < this.chunks.length; i++) {
			for (Class<?> aClass : this.chunks[i].getTargetClasses()) {
				this.mappings.put(aClass, (byte) i);
			}
		}
	}

	void addChunkMapping(Class<?> tag, byte pos) {
		this.dashTag2chunkMappings.put(tag, pos);
		final DashObject declaredAnnotation = tag.getDeclaredAnnotation(DashObject.class);
		if (declaredAnnotation != null) {
			this.target2chunkMappings.put(declaredAnnotation.value(), pos);
		} else {
			throw new RuntimeException("No DashObject annotation for " + tag.getSimpleName());
		}
	}

	@SuppressWarnings("unchecked")
	public <R, D extends Dashable<R>> AbstractWriteChunk<R, D> getChunk(Class<D> dashType) {
		return (AbstractWriteChunk<R, D>) this.chunks[this.dashTag2chunkMappings.getByte(dashType)];
	}

	public <R, D extends Dashable<R>> int addDirect(AbstractWriteChunk<R, D> chunk, R object) {
		final int objectPos = chunk.add(object);
		final int pointer = createPointer(objectPos, chunk.pos);
		((Object2IntMap<R>) this.dedup).put(object, pointer);
		return pointer;
	}

	@SuppressWarnings("unchecked")
	public <R> int add(R object) {
		if (this.dedup.containsKey(object)) {
			return this.dedup.getInt(object);
		}
		var targetClass = object.getClass();
		byte chunkPos = this.mappings.getOrDefault(targetClass, (byte) -1);

		if (chunkPos == -1) {
			for (var targetChunk : this.target2chunkMappings.object2ByteEntrySet()) {
				if (targetChunk.getKey().isAssignableFrom(targetClass)) {
					chunkPos = targetChunk.getByteValue();
					break;
				}
			}
		}

		if (chunkPos == -1) {
			throw new RuntimeException("Could not find a ChunkWriter for " + targetClass);
		}

		var chunk = (AbstractWriteChunk<R, ?>) this.chunks[chunkPos];
		final var objectPos = chunk.add(object);
		final int pointer = createPointer(objectPos, chunkPos);
		((Object2IntMap<R>) this.dedup).put(object, pointer);
		return pointer;
	}
}
