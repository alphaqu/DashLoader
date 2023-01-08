package dev.notalpha.dashloader.misc.duck;

import java.util.Map;

import dev.notalpha.dashloader.client.sprite.DashAtlasData;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.MutablePair;

public interface SpriteAtlasTextureDuck {
	void dashSetupSprites(Map<Identifier, Sprite> sprites);
	void dashAddData(DashAtlasData data);

	Map<Identifier, MutablePair<Sprite, Sprite.Info>> getCachedSprites();
}
