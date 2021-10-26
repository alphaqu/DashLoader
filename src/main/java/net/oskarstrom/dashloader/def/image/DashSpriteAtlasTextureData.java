package net.oskarstrom.dashloader.def.image;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.oskarstrom.dashloader.def.mixin.accessor.SpriteAtlasTextureDataAccessor;

@Data
public record DashSpriteAtlasTextureData(int width, int height, int maxLevel) {
	public DashSpriteAtlasTextureData(SpriteAtlasTexture.Data data) {
		this((SpriteAtlasTextureDataAccessor) data);
	}

	private DashSpriteAtlasTextureData(SpriteAtlasTextureDataAccessor access) {
		this(access.getWidth(), access.getHeight(), access.getMaxLevel());
	}
}
