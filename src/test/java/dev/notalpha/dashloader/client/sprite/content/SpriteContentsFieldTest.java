package dev.notalpha.dashloader.client.sprite.content;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.notalpha.dashloader.io.Serializer;
import dev.notalpha.hyphen.io.ByteBufferIO;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpriteContentsFieldTest {
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

	@Test
	void mipmapStrategySurvivesTheRoundTrip() {
		for (MipmapStrategy strategy : MipmapStrategy.values()) {
			DashSpriteContents sprite =
					new DashSpriteContents(1, 2, null, 16, 16, strategy, 0.0F, EMPTY, EMPTY);

			DashSpriteContents loaded = roundTrip(sprite);

			assertEquals(strategy, loaded.mipmapStrategy);
		}
	}

	@Test
	void alphaCutoffBiasSurvivesTheRoundTrip() {
		DashSpriteContents sprite =
				new DashSpriteContents(1, 2, null, 16, 16, MipmapStrategy.MEAN, 0.5F, EMPTY, EMPTY);

		DashSpriteContents loaded = roundTrip(sprite);

		assertEquals(0.5F, loaded.alphaCutoffBias);
	}

	@Test
	void dimensionsSurviveTheRoundTrip() {
		DashSpriteContents sprite =
				new DashSpriteContents(7, 8, null, 64, 32, MipmapStrategy.CUTOUT, -0.25F, EMPTY, EMPTY);

		DashSpriteContents loaded = roundTrip(sprite);

		assertEquals(7, loaded.id);
		assertEquals(8, loaded.image);
		assertEquals(64, loaded.width);
		assertEquals(32, loaded.height);
		assertEquals(MipmapStrategy.CUTOUT, loaded.mipmapStrategy);
		assertEquals(-0.25F, loaded.alphaCutoffBias);
	}

	@Test
	void metadataArraysSurviveTheRoundTrip() {
		DashSpriteContents sprite = new DashSpriteContents(1, 2, null, 16, 16, MipmapStrategy.AUTO, 0.0F,
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
		JsonElement json = JsonParser.parseString(
				"{\"scaling\":{\"type\":\"nine_slice\",\"width\":100,\"height\":100,\"border\":9}}");
		GuiMetadataSection section =
				GuiMetadataSection.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();

		String encoded = ((java.util.Optional<?>) invoke("encodeMetadata",
				new Class<?>[]{MetadataSectionType.class, Object.class}, GuiMetadataSection.TYPE, section))
				.map(String.class::cast).orElseThrow();
		Object decoded = ((java.util.Optional<?>) invoke("decodeMetadata",
				new Class<?>[]{MetadataSectionType.class, String.class}, GuiMetadataSection.TYPE, encoded))
				.orElseThrow();

		assertEquals(section, decoded);
	}

	@Test
	void nineSliceMetadataSurvivesTheCache() throws Exception {
		JsonElement json = JsonParser.parseString(
				"{\"scaling\":{\"type\":\"nine_slice\",\"width\":100,\"height\":100,\"border\":9}}");
		GuiMetadataSection section =
				GuiMetadataSection.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();

		String encoded = ((java.util.Optional<?>) invoke("encodeMetadata",
				new Class<?>[]{MetadataSectionType.class, Object.class}, GuiMetadataSection.TYPE, section))
				.map(String.class::cast).orElseThrow();

		DashSpriteContents sprite = new DashSpriteContents(1, 2, null, 100, 100, MipmapStrategy.AUTO, 0.0F,
				new String[]{GuiMetadataSection.TYPE.name()}, new String[]{encoded});

		DashSpriteContents loaded = roundTrip(sprite);

		assertEquals(GuiMetadataSection.TYPE.name(), loaded.metadataNames[0]);
		Object decoded = ((java.util.Optional<?>) invoke("decodeMetadata",
				new Class<?>[]{MetadataSectionType.class, String.class},
				GuiMetadataSection.TYPE, loaded.metadataValues[0])).orElseThrow();
		assertEquals(section, decoded);
		assertTrue(loaded.metadataNames[0].equals("gui"));
	}
}
