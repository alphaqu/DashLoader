package dev.notalpha.dashloader.mixin.accessor;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Transparency;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SpriteContents.class)
public interface SpriteContentsAccessor {
	@Accessor
	NativeImage getOriginalImage();

	@Accessor
	@Mutable
	void setOriginalImage(NativeImage image);

	@Accessor
	SpriteContents.AnimatedTexture getAnimatedTexture();

	@Accessor
	@Mutable
	void setAnimatedTexture(SpriteContents.AnimatedTexture animation);

	@Accessor
	@Mutable
	void setByMipLevel(NativeImage[] mipmapLevelsImages);

	@Accessor
	@Mutable
	void setName(Identifier id);

	@Accessor
	@Mutable
	void setWidth(int width);

	@Accessor
	@Mutable
	void setHeight(int height);

	@Accessor
	@Mutable
	void setTransparency(Transparency transparency);

	@Accessor
	List<MetadataSectionType.WithValue<?>> getAdditionalMetadata();

	@Accessor
	@Mutable
	void setAdditionalMetadata(List<MetadataSectionType.WithValue<?>> metadata);

	@Accessor
	MipmapStrategy getMipmapStrategy();

	@Accessor
	@Mutable
	void setMipmapStrategy(MipmapStrategy strategy);

	@Accessor
	float getAlphaCutoffBias();

	@Accessor
	@Mutable
	void setAlphaCutoffBias(float bias);
}