package dev.quantumfusion.dashloader.def.mixin.accessor;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Sprite.class)
public interface SpriteAccessor {

	@Accessor
	@Mutable
	void setAtlas(SpriteAtlasTexture atlas);

	@Accessor
	@Mutable
	void setId(Identifier id);

	@Accessor
	@Mutable
	void setAnimation(Sprite.Animation animation);

	@Accessor
	@Mutable
	Sprite.Animation getAnimation();

	@Accessor
	NativeImage[] getImages();

	@Accessor
	@Mutable
	void setImages(NativeImage[] images);

	@Accessor("x")
	@Mutable
	void setX(int x);


	@Accessor("y")
	@Mutable
	void setY(int y);


	@Accessor
	@Mutable
	void setUMin(float uMin);

	@Accessor
	@Mutable
	void setUMax(float uMax);


	@Accessor
	@Mutable
	void setVMin(float vMin);


	@Accessor
	@Mutable
	void setVMax(float vMax);


	@Accessor
	@Mutable
	void setWidth(int width);

	@Accessor
	@Mutable
	void setHeight(int height);

}
