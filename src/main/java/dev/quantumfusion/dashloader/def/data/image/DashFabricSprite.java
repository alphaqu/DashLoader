package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import net.fabricmc.fabric.impl.client.texture.FabricSprite;


@DashObject(FabricSprite.class)
public class DashFabricSprite extends DashSpriteImpl implements DashSprite {
	public DashFabricSprite(DashSpriteAnimation animation, int[] images, int x, int y, int width, int height, float uMin, float uMax, float vMin, float vMax) {
		super(animation, images, x, y, width, height, uMin, uMax, vMin, vMax);
	}

	public DashFabricSprite(FabricSprite sprite, DashRegistryWriter writer) {
		super(sprite, writer);
	}
}
