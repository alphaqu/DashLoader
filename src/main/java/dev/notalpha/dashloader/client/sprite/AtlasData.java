package dev.notalpha.dashloader.client.sprite;

import dev.notalpha.dashloader.io.data.collection.IntIntList;
import dev.notalpha.dashloader.mixin.accessor.SpriteAtlasTextureAccessor;
import dev.notalpha.dashloader.mixin.accessor.SpriteAtlasTextureDataAccessor;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import java.util.Map;

public class AtlasData {
	public final int width;
	public final int height;
	public final int maxLevel;
	public final Identifier id;
	public final Map<Identifier, Sprite> sprites;

	public AtlasData(int width, int height, int maxLevel, Identifier id, Map<Identifier, Sprite> sprites) {
		this.width = width;
		this.height = height;
		this.maxLevel = maxLevel;
		this.id = id;
		this.sprites = sprites;
	}

	public AtlasData(SpriteAtlasTexture texture, SpriteAtlasTexture.Data data) {
		var dataAccess = (SpriteAtlasTextureDataAccessor) data;

		this.width = dataAccess.getWidth();
		this.height = dataAccess.getHeight();
		this.maxLevel = dataAccess.getMaxLevel();
		this.id = texture.getId();
		this.sprites = ((SpriteAtlasTextureAccessor) texture).getSprites();
	}
}
