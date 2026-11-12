package dev.notalpha.dashloader.misc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheStatsTest {
	@Test
	void formatsBytes() {
		assertEquals("512 B", CacheStats.formatBytes(512));
		assertEquals("1.0 kB", CacheStats.formatBytes(1024));
		assertEquals("1.0 MB", CacheStats.formatBytes(1024L * 1024L));
		assertEquals("1.5 MB", CacheStats.formatBytes(1024L * 1024L * 3L / 2L));
		assertEquals("1.0 GB", CacheStats.formatBytes(1024L * 1024L * 1024L));
	}

	@Test
	void noStatsBeforeAnythingWasRecorded() {
		CacheStats.clear();
		assertTrue(!CacheStats.hasStats());
	}

	@Test
	void formatIncludesComparisonWhenVanillaTimeIsKnown() {
		CacheStats.clear();
		CacheStats.recordLoad(5300, 43000, 142L * 1024L * 1024L, 17);

		String formatted = CacheStats.format();
		assertTrue(formatted.contains("5.3s"), formatted);
		assertTrue(formatted.contains("43.0s"), formatted);
		assertTrue(formatted.contains("142.0 MB"), formatted);
		assertTrue(formatted.contains("17 fragments"), formatted);
	}

	@Test
	void formatOmitsComparisonWhenVanillaTimeIsUnknown() {
		CacheStats.clear();
		CacheStats.recordLoad(5300, -1, 1024L, 1);

		String formatted = CacheStats.format();
		assertTrue(formatted.contains("5.3s"), formatted);
		assertTrue(formatted.contains("1 fragment"), formatted);
		assertTrue(!formatted.contains("(vanilla"), formatted);
	}

	@Test
	void directorySizeOfMissingDirectoryIsNegative() {
		assertEquals(-1L, CacheStats.directorySize(null));
		assertEquals(-1L, CacheStats.directorySize(java.nio.file.Path.of("does", "not", "exist")));
	}

	@Test
	void statsSurviveARoundTripThroughDisk() throws Exception {
		java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("dashloader-stats-test");
		try {
			CacheStats.writeVanillaTiming(dir, 4300);
			assertEquals(4300L, CacheStats.readVanillaTiming(dir));
		} finally {
			java.nio.file.Files.deleteIfExists(dir.resolve("stats.json"));
			java.nio.file.Files.deleteIfExists(dir);
		}
	}

	@Test
	void missingVanillaTimingIsReportedAsUnknown() throws Exception {
		java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("dashloader-stats-empty");
		try {
			assertEquals(-1L, CacheStats.readVanillaTiming(dir));
		} finally {
			java.nio.file.Files.deleteIfExists(dir);
		}
	}
}