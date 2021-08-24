package net.oskarstrom.dashloader.def.datatest;

import net.oskarstrom.dashloader.def.TestUtils;
import net.oskarstrom.dashloader.def.image.DashImage;
import org.junit.jupiter.api.Test;

public class ImageDataTest {

	@Test
	public void testImageSerialization() {
		TestUtils.testCreation(DashImage.class);
	}
}
