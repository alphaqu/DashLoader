package dev.quantumfusion.dashloader.def.util.mixins;

import dev.quantumfusion.dashloader.def.data.image.DashSpriteAtlasTextureData;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface SpriteAtlasTextureDuck {
	void dashLoaded(DashSpriteAtlasTextureData data, Map<Identifier, Sprite> sprites);
}
