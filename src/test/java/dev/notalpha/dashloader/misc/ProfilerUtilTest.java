package dev.notalpha.dashloader.misc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfilerUtilTest {
	@Test
	void formatsMillisecondsBelowThreeSeconds() {
		assertEquals("0ms", ProfilerUtil.getTimeString(0));
		assertEquals("400ms", ProfilerUtil.getTimeString(400));
		assertEquals("2999ms", ProfilerUtil.getTimeString(2999));
		assertEquals("1293ms", ProfilerUtil.getTimeString(1293));
	}

	@Test
	void formatsSecondsWithOneDecimalAboveThreeSeconds() {
		assertEquals("3.0s", ProfilerUtil.getTimeString(3000));
		assertEquals("5.3s", ProfilerUtil.getTimeString(5300));
		assertEquals("12.9s", ProfilerUtil.getTimeString(12930));
		assertEquals("59.9s", ProfilerUtil.getTimeString(59900));
	}

	@Test
	void formatsMinutesAndSeconds() {
		assertEquals("1m 0s", ProfilerUtil.getTimeString(60000));
		assertEquals("4m 42s", ProfilerUtil.getTimeString(4 * 60_000 + 42_000));
	}

	@Test
	void measuresElapsedTimeFromStart() {
		long start = System.currentTimeMillis();
		String formatted = ProfilerUtil.getTimeStringFromStart(start);
		assertTrue(formatted.endsWith("ms"), "expected a millisecond value, got " + formatted);
	}
}