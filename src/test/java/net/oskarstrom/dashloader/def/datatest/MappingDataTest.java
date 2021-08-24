package net.oskarstrom.dashloader.def.datatest;

import net.oskarstrom.dashloader.def.TestUtils;
import net.oskarstrom.dashloader.def.data.serialize.mapping.*;
import net.oskarstrom.dashloader.def.image.shader.*;
import org.junit.jupiter.api.Test;

public class MappingDataTest {

	@Test
	public void testDashBlockStateDataSerialization() {
		TestUtils.testCreation(DashBlockStateData.class);
	}

	@Test
	public void testDashFontManagerDataSerialization() {
		TestUtils.testCreation(DashFontManagerData.class);
	}

	@Test
	public void testDashModelDataSerialization() {
		TestUtils.testCreation(DashModelData.class);
	}

	@Test
	public void testDashParticleDataSerialization() {
		TestUtils.testCreation(DashParticleData.class);
	}

	@Test
	public void testDashSplashTextDataSerialization() {
		TestUtils.testCreation(DashSplashTextData.class);
	}

	@Test
	public void testDashSpriteAtlasDataSerialization() {
		TestUtils.testCreation(DashSpriteAtlasData.class);
	}

	@Test
	public void testDashShaderDataSerialization() {
		TestUtils.testCreation(DashGlUniform.class);
		TestUtils.testCreation(DashProgram.class);
		TestUtils.testCreation(VertexFormatsHelper.Value.class);
		TestUtils.testCreation(DashGlBlendState.class);
		TestUtils.testCreation(DashShader.class);
		TestUtils.testCreation(DashShaderData.class);
	}
}
