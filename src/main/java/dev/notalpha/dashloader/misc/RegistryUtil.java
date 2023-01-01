package dev.notalpha.dashloader.misc;

public class RegistryUtil {
	public static int createId(int objectPos, byte chunkPos) {
		if (chunkPos > 0b111111) {
			throw new IllegalStateException("Chunk pos is too big. " + chunkPos + " > " + 0x3f);
		}
		if (objectPos > 0x3ffffff) {
			throw new IllegalStateException("Object pos is too big. " + objectPos + " > " + 0x3ffffff);
		}
		return objectPos << 6 | (chunkPos & 0x3f);
	}

	public static byte getChunkId(int id) {
		return (byte) (id & 0x3f);
	}

	public static int getObjectId(int id) {
		return id >>> 6;
	}
}
