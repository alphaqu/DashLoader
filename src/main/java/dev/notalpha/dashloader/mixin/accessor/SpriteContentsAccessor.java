package dev.notalpha.dashloader.mixin.accessor;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

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
	java.util.List<MetadataSectionType.WithValue<?>> getAdditionalMetadata();

	@Accessor
	MipmapStrategy getMipmapStrategy();

	@Accessor
	float getAlphaCutoffBias();

	@Accessor
	@Mutable
	void setAdditionalMetadata(java.util.List<MetadataSectionType.WithValue<?>> additionalMetadata);

	@Accessor
	@Mutable
	void setMipmapStrategy(MipmapStrategy strategy);

	@Accessor
	@Mutable
	void setAlphaCutoffBias(float cutoffBias);
}
