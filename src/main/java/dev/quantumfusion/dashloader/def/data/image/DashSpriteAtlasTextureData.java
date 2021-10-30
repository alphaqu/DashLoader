package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAtlasTextureDataAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.texture.SpriteAtlasTexture;

@Data
public record DashSpriteAtlasTextureData(int width, int height, int maxLevel) {
	public DashSpriteAtlasTextureData(SpriteAtlasTexture.Data data) {
		this((SpriteAtlasTextureDataAccessor) data);
	}

	private DashSpriteAtlasTextureData(SpriteAtlasTextureDataAccessor access) {
		this(access.getWidth(), access.getHeight(), access.getMaxLevel());
	}
}
