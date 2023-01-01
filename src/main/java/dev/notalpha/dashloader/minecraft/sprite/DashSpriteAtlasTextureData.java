package dev.notalpha.dashloader.minecraft.sprite;

import dev.notalpha.dashloader.mixin.accessor.SpriteLoaderStitchResultAccessor;
import net.minecraft.client.texture.SpriteLoader;

public final class DashSpriteAtlasTextureData {
	public final int width;
	public final int height;
	public final int mipLevel;

	public DashSpriteAtlasTextureData(int width, int height, int mipLevel) {
		this.width = width;
		this.height = height;
		this.mipLevel = mipLevel;
	}

	public DashSpriteAtlasTextureData(SpriteLoader.StitchResult data) {
		this((SpriteLoaderStitchResultAccessor) (Object) data);
	}

	private DashSpriteAtlasTextureData(SpriteLoaderStitchResultAccessor access) {
		this(access.getWidth(), access.getHeight(), access.getMipLevel());
	}
}
