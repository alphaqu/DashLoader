package dev.quantumfusion.dashloader.data.image;

import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import net.fabricmc.fabric.impl.client.texture.FabricSprite;


@DashObject(FabricSprite.class)
public final class DashFabricSprite extends DashSpriteImpl implements DashSprite {

	public DashFabricSprite(int atlasId, DashSpriteContents contents, int x, int y, float uMin, float uMax, float vMin, float vMax) {
		super(atlasId, contents, x, y, uMin, uMax, vMin, vMax);
	}

	public DashFabricSprite(FabricSprite sprite, RegistryWriter writer) {
		super(sprite, writer);
	}
}
