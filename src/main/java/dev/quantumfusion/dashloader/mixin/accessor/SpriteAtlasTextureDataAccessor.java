package dev.quantumfusion.dashloader.mixin.accessor;

import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteAtlasTexture.Data.class)
public interface SpriteAtlasTextureDataAccessor {

	@Accessor
	int getWidth();

	@Accessor
	int getHeight();

	@Accessor
	int getMaxLevel();


}
