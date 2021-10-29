package dev.quantumfusion.dashloader.def.datatest;

import dev.quantumfusion.dashloader.def.TestUtils;
import dev.quantumfusion.dashloader.def.data.model.DashBasicBakedModel;
import dev.quantumfusion.dashloader.def.data.model.DashBuiltinBakedModel;
import dev.quantumfusion.dashloader.def.data.model.DashMultipartBakedModel;
import dev.quantumfusion.dashloader.def.data.model.DashWeightedBakedModel;
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
