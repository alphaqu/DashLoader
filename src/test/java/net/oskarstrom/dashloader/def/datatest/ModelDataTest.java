package net.oskarstrom.dashloader.def.datatest;

import net.oskarstrom.dashloader.def.TestUtils;
import net.oskarstrom.dashloader.def.model.DashBasicBakedModel;
import net.oskarstrom.dashloader.def.model.DashBuiltinBakedModel;
import net.oskarstrom.dashloader.def.model.DashMultipartBakedModel;
import net.oskarstrom.dashloader.def.model.DashWeightedBakedModel;
import org.junit.jupiter.api.Test;

public class ModelDataTest {
	@Test
	public void testBasicModelSerialization() {
		TestUtils.testCreation(DashBasicBakedModel.class);
	}

	@Test
	public void testMultiModelSerialization() {
		TestUtils.testCreation(DashMultipartBakedModel.class);
	}

	@Test
	public void testWeightedModelSerialization() {
		TestUtils.testCreation(DashWeightedBakedModel.class);
	}

	@Test
	public void testBuiltinModelSerialization() {
		TestUtils.testCreation(DashBuiltinBakedModel.class);
	}

}
