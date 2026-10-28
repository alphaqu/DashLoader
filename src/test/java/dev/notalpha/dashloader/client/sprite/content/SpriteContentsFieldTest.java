package dev.notalpha.dashloader.client.sprite.content;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.notalpha.dashloader.io.Serializer;
import dev.notalpha.hyphen.io.ByteBufferIO;
import net.minecraft.client.resource.metadata.GuiResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpriteContentsFieldTest {
	private static final String NINE_SLICE =
			"{\"scaling\":{\"type\":\"nine_slice\",\"width\":100,\"height\":100,\"border\":9}}";
	private static final String[] EMPTY = new String[0];

	private static DashSpriteContents roundTrip(DashSpriteContents sprite) {
		Serializer<DashSpriteContents> serializer = new Serializer<>(DashSpriteContents.class);
		int size = (int) serializer.measure(sprite);

		ByteBuffer buffer = ByteBuffer.allocate(size);
		ByteBufferIO io = ByteBufferIO.wrap(buffer);
		serializer.put(io, sprite);
		io.rewind();
		return serializer.get(io);
	}

	private static Object invoke(String name, Class<?>[] signature, Object... args) throws Exception {
		Method method = DashSpriteContents.class.getDeclaredMethod(name, signature);
		method.setAccessible(true);
		return method.invoke(null, args);
	}

	private static String encode(GuiResourceMetadata section) throws Exception {
		return ((java.util.Optional<?>) invoke("encodeMetadata",
				new Class<?>[]{ResourceMetadataSerializer.class, Object.class},
				GuiResourceMetadata.SERIALIZER, section)).map(String.class::cast).orElseThrow();
	}

	private static Object decode(String json) throws Exception {
		return ((java.util.Optional<?>) invoke("decodeMetadata",
				new Class<?>[]{ResourceMetadataSerializer.class, String.class},
				GuiResourceMetadata.SERIALIZER, json)).orElseThrow();
	}

	private static GuiResourceMetadata nineSlice() {
		JsonElement json = JsonParser.parseString(NINE_SLICE);
		return GuiResourceMetadata.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();
	}

	@Test
	void dimensionsSurviveTheRoundTrip() {
		DashSpriteContents sprite = new DashSpriteContents(7, 8, null, 64, 32, EMPTY, EMPTY);

		DashSpriteContents loaded = roundTrip(sprite);

		assertEquals(7, loaded.id);
		assertEquals(8, loaded.image);
		assertEquals(64, loaded.width);
		assertEquals(32, loaded.height);
	}

	@Test
	void metadataArraysSurviveTheRoundTrip() {
		DashSpriteContents sprite = new DashSpriteContents(1, 2, null, 16, 16,
				new String[]{"gui", "texture"}, new String[]{"{\"scaling\":null}", "{\"blur\":true}"});

		DashSpriteContents loaded = roundTrip(sprite);

		assertEquals(2, loaded.metadataNames.length);
		assertEquals(2, loaded.metadataValues.length);
		assertEquals("gui", loaded.metadataNames[0]);
		assertEquals("{\"scaling\":null}", loaded.metadataValues[0]);
		assertEquals("texture", loaded.metadataNames[1]);
	}

	@Test
	void nineSliceMetadataSurvivesTheCodecRoundTrip() throws Exception {
		GuiResourceMetadata section = nineSlice();

		String encoded = encode(section);
		Object decoded = decode(encoded);

		assertEquals(section, decoded);
	}

	@Test
	void nineSliceMetadataSurvivesTheCache() throws Exception {
		GuiResourceMetadata section = nineSlice();

		DashSpriteContents sprite = new DashSpriteContents(1, 2, null, 100, 100,
				new String[]{GuiResourceMetadata.SERIALIZER.name()}, new String[]{encode(section)});

		DashSpriteContents loaded = roundTrip(sprite);

		assertEquals("gui", loaded.metadataNames[0]);
		assertEquals(section, decode(loaded.metadataValues[0]));
	}
}
