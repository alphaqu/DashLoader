package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import net.fabricmc.fabric.impl.client.texture.FabricSprite;


@DashObject(FabricSprite.class)
public class DashFabricSprite extends DashSpriteImpl implements DashSprite {
	public DashFabricSprite(DashSpriteAnimation animation, int[] images, int x, int y, int width, int height, float uMin, float uMax, float vMin, float vMax) {
		super(animation, images, x, y, width, height, uMin, uMax, vMin, vMax);
	}

	public DashFabricSprite(FabricSprite sprite, RegistryWriter writer) {
		super(sprite, writer);
	}
}
