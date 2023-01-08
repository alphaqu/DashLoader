package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Set;

@Mixin(SpriteAtlasTexture.Data.class)
public interface SpriteAtlasTextureDataAccessor {

	@Accessor
	int getWidth();

	@Accessor
	int getHeight();

	@Accessor
	int getMaxLevel();

	@Accessor
	Set<Identifier> getSpriteIds();

	@Accessor
	List<Sprite> getSprites();

}
