package net.oskarstrom.dashloader.def.image;

import net.oskarstrom.dashloader.def.mixin.accessor.SpriteAtlasTextureDataAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.texture.SpriteAtlasTexture;

public class DashSpriteAtlasTextureData {
	@Serialize(order = 0)
	public final int width;
	@Serialize(order = 1)
	public final int height;
	@Serialize(order = 2)
	public final int maxLevel;

	public DashSpriteAtlasTextureData(@Deserialize("width") int width,
									  @Deserialize("height") int height,
									  @Deserialize("maxLevel") int maxLevel) {
		this.width = width;
		this.height = height;
		this.maxLevel = maxLevel;
	}

	public DashSpriteAtlasTextureData(SpriteAtlasTexture.Data data) {
		SpriteAtlasTextureDataAccessor access = ((SpriteAtlasTextureDataAccessor) data);
		width = access.getWidth();
		height = access.getHeight();
		maxLevel = access.getMaxLevel();
	}
}
