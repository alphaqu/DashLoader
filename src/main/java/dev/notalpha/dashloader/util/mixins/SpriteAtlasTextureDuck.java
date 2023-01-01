package dev.notalpha.dashloader.util.mixins;

import dev.notalpha.dashloader.minecraft.sprite.DashSpriteAtlasTextureData;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface SpriteAtlasTextureDuck {
	void dashLoaded(DashSpriteAtlasTextureData data, Map<Identifier, Sprite> sprites);

}
