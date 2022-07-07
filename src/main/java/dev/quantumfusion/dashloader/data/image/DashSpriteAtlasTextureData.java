package dev.quantumfusion.dashloader.data.image;

import dev.quantumfusion.dashloader.mixin.accessor.SpriteAtlasTextureDataAccessor;
import net.minecraft.client.texture.SpriteAtlasTexture;

public final class DashSpriteAtlasTextureData {
	public final int width;
	public final int height;
	public final int mipLevel;

	public DashSpriteAtlasTextureData(int width, int height, int mipLevel) {
		this.width = width;
		this.height = height;
		this.mipLevel = mipLevel;
	}

	public DashSpriteAtlasTextureData(SpriteAtlasTexture.Data data) {
		this((SpriteAtlasTextureDataAccessor) data);
	}

	private DashSpriteAtlasTextureData(SpriteAtlasTextureDataAccessor access) {
		this(access.getWidth(), access.getHeight(), access.getMaxLevel());
	}
}
