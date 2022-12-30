package dev.quantumfusion.dashloader.util;

import dev.quantumfusion.dashloader.minecraft.model.predicates.BooleanSelector;
import net.minecraft.client.render.model.json.MultipartModelSelector;

public class RegistryUtil {
	public static MultipartModelSelector preparePredicate(final MultipartModelSelector selector) {
		if (selector == MultipartModelSelector.TRUE || selector == MultipartModelSelector.FALSE) {
			return new BooleanSelector(selector);
		}
		return selector;
	}

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
