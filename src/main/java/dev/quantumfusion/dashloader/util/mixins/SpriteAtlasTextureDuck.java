package dev.quantumfusion.dashloader.util.mixins;

import dev.quantumfusion.dashloader.data.image.DashSpriteAtlasTextureData;
import java.util.Map;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.MutablePair;

public interface SpriteAtlasTextureDuck {
	void dashLoaded(DashSpriteAtlasTextureData data, Map<Identifier, Sprite> sprites);

	Map<Identifier, MutablePair<Sprite, Sprite.Info>> getCachedSprites();
}
