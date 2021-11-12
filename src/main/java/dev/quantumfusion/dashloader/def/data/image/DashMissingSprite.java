package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import net.minecraft.client.texture.MissingSprite;

@DashObject(MissingSprite.class)
public class DashMissingSprite extends DashSpriteImpl implements DashSprite {
	public DashMissingSprite(DashSpriteAnimation animation, int[] images, int x, int y, int width, int height, float uMin, float uMax, float vMin, float vMax) {
		super(animation, images, x, y, width, height, uMin, uMax, vMin, vMax);
	}

	public DashMissingSprite(MissingSprite sprite, RegistryWriter writer) {
		super(sprite, writer);
	}
}
