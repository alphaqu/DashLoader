package dev.quantumfusion.dashloader.data.image;

import dev.quantumfusion.dashloader.mixin.accessor.SpriteAtlasTextureDataAccessor;
import net.minecraft.client.texture.SpriteAtlasTexture;

public record DashSpriteAtlasTextureData(int width, int height, int mipLevel) {
	public DashSpriteAtlasTextureData(SpriteAtlasTexture.Data data) {
		this((SpriteAtlasTextureDataAccessor) data);
	}

	private DashSpriteAtlasTextureData(SpriteAtlasTextureDataAccessor access) {
		this(access.getWidth(), access.getHeight(), access.getMaxLevel());
	}
}
