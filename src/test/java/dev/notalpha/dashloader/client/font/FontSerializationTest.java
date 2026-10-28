package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.io.Serializer;
import dev.notalpha.hyphen.io.ByteBufferIO;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FontSerializationTest {
	private static DashTrueTypeFont roundTrip(DashTrueTypeFont font) {
		Serializer<DashTrueTypeFont> serializer = new Serializer<>(DashTrueTypeFont.class);
		int size = (int) serializer.measure(font);

		ByteBuffer buffer = ByteBuffer.allocate(size);
		ByteBufferIO io = ByteBufferIO.wrap(buffer);
		serializer.put(io, font);
		io.rewind();
		return serializer.get(io);
	}

	@Test
	void glyphMapSurvivesTheRoundTrip() {
		int[] glyphs = {97, 3, 98, 0, 99, 4123};
		DashTrueTypeFont font = new DashTrueTypeFont(new byte[]{1, 2, 3, 4}, 1.0F, new int[]{65, 66}, 8, 0.5F,
				-0.25F, glyphs);

		DashTrueTypeFont loaded = roundTrip(font);

		assertArrayEquals(glyphs, loaded.glyphMap);
	}

	@Test
	void everyOtherFieldSurvivesTheRoundTrip() {
		byte[] data = new byte[]{7, 8, 9};
		int[] excluded = {10, 20, 30};
		int[] glyphs = {65, 7, 66, 8};
		DashTrueTypeFont font = new DashTrueTypeFont(data, 2.0F, excluded, 16, 1.5F, -3.25F, glyphs);

		DashTrueTypeFont loaded = roundTrip(font);

		assertArrayEquals(data, loaded.fontData);
		assertEquals(2.0F, loaded.oversample);
		assertArrayEquals(excluded, loaded.excludedCharacters);
		assertEquals(16, loaded.size);
		assertEquals(1.5F, loaded.shiftX);
		assertEquals(-3.25F, loaded.shiftY);
	}

	@Test
	void fontWithoutAnyGlyphIsKeptDistinctFromAFontWithGlyphs() {
		DashTrueTypeFont empty = roundTrip(
				new DashTrueTypeFont(new byte[]{1}, 1.0F, new int[0], 8, 0.0F, 0.0F, new int[0]));
		DashTrueTypeFont filled = roundTrip(
				new DashTrueTypeFont(new byte[]{1}, 1.0F, new int[0], 8, 0.0F, 0.0F, new int[]{65, 1}));

		assertArrayEquals(new int[0], empty.glyphMap);
		assertArrayEquals(new int[]{65, 1}, filled.glyphMap);
	}

	@Test
	void measureGrowsWithTheGlyphMap() {
		Serializer<DashTrueTypeFont> serializer = new Serializer<>(DashTrueTypeFont.class);

		int[] small = new int[100];
		int[] large = new int[2000];
		for (int i = 0; i < large.length; i++) {
			large[i] = i;
			small[i % small.length] = i;
		}

		long withoutGlyphs = serializer.measure(
				new DashTrueTypeFont(new byte[10], 1.0F, new int[0], 8, 0.0F, 0.0F, new int[0]));
		long smallMap = serializer.measure(new DashTrueTypeFont(new byte[10], 1.0F, new int[0], 8, 0.0F, 0.0F, small));
		long largeMap = serializer.measure(new DashTrueTypeFont(new byte[10], 1.0F, new int[0], 8, 0.0F, 0.0F, large));

		assertTrue(largeMap > smallMap, largeMap + " should be bigger than " + smallMap);
		assertTrue(smallMap > withoutGlyphs, smallMap + " should be bigger than " + withoutGlyphs);
		assertEquals(smallMap - withoutGlyphs, small.length * 4L);
		assertEquals(largeMap - withoutGlyphs, large.length * 4L);
	}
}