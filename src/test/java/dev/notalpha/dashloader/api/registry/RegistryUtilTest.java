package dev.notalpha.dashloader.api.registry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistryUtilTest {
	@Test
	void packsAndUnpacksId() {
		int id = RegistryUtil.createId(1234, (byte) 7);
		assertEquals(7, RegistryUtil.getChunkId(id));
		assertEquals(1234, RegistryUtil.getObjectId(id));
	}

	@Test
	void roundTripsEveryChunkAndObjectCombination() {
		for (int chunk = 0; chunk <= 0x3f; chunk++) {
			for (int object : new int[]{0, 1, 63, 64, 1000, 0x3ffffff}) {
				int id = RegistryUtil.createId(object, (byte) chunk);
				assertEquals(chunk, RegistryUtil.getChunkId(id), "chunk " + chunk + " object " + object);
				assertEquals(object, RegistryUtil.getObjectId(id), "chunk " + chunk + " object " + object);
			}
		}
	}

	@Test
	void idsAreUnique() {
		for (int chunk = 0; chunk <= 0x3f; chunk++) {
			for (int object = 0; object < 5000; object++) {
				int id = RegistryUtil.createId(object, (byte) chunk);
				assertEquals(object, RegistryUtil.getObjectId(id));
				assertEquals(chunk, RegistryUtil.getChunkId(id));
			}
		}
	}

	@Test
	void objectPositionUsesTheHighBitsAndChunkTheLowBits() {
		assertEquals(1, RegistryUtil.getObjectId(RegistryUtil.createId(1, (byte) 0)));
		assertEquals(0, RegistryUtil.getObjectId(RegistryUtil.createId(0, (byte) 0)));
		assertEquals(0, RegistryUtil.getObjectId(RegistryUtil.createId(0, (byte) 1)));
		assertEquals(1, RegistryUtil.getChunkId(RegistryUtil.createId(0, (byte) 1)));
	}

	@Test
	void rejectsOutOfRangeChunk() {
		assertThrows(IllegalStateException.class, () -> RegistryUtil.createId(0, (byte) 0x40));
	}

	@Test
	void rejectsOutOfRangeObject() {
		assertThrows(IllegalStateException.class, () -> RegistryUtil.createId(0x4000000, (byte) 0));
	}

	@Test
	void rejectsNegativeObjectPosition() {
		assertThrows(IllegalStateException.class, () -> RegistryUtil.createId(-1, (byte) 0));
	}
}