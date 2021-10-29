package dev.quantumfusion.dashloader.def.datatest;

import dev.quantumfusion.dashloader.def.TestUtils;
import dev.quantumfusion.dashloader.def.data.image.DashImage;
import org.junit.jupiter.api.Test;

public class ImageDataTest {

	@Test
	public void testImageSerialization() {
		TestUtils.testCreation(DashImage.class);
	}
}
