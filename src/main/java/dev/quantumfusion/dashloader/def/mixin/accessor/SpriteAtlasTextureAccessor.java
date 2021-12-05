package dev.quantumfusion.dashloader.def.mixin.accessor;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(SpriteAtlasTexture.class)
public interface SpriteAtlasTextureAccessor {

	@Accessor
	Map<Identifier, Sprite> getSprites();

	@Accessor
	@Mutable
	void setSprites(Map<Identifier, Sprite> sprites);

	@Accessor
	@Mutable
	void setAnimatedSprites(List<TextureTickListener> animatedSprites);

	@Accessor
	@Mutable
	void setSpritesToLoad(Set<Identifier> spritesToLoad);

	@Accessor
	Identifier getId();

	@Accessor
	@Mutable
	void setId(Identifier id);

	@Accessor
	int getMaxTextureSize();

	@Accessor
	@Mutable
	void setMaxTextureSize(int maxTextureSize);

}
