package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.client.texture.MipmapStrategy;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteContents.class)
public interface SpriteContentsAccessor {
	@Accessor
	NativeImage getImage();

	@Accessor
	@Mutable
	void setImage(NativeImage image);

	@Accessor
	SpriteContents.Animation getAnimation();

	@Accessor
	@Mutable
	void setAnimation(SpriteContents.Animation animation);

	@Accessor
	@Mutable
	void setMipmapLevelsImages(NativeImage[] mipmapLevelsImages);

	@Accessor
	@Mutable
	void setId(Identifier id);

	@Accessor
	@Mutable
	void setWidth(int width);

	@Accessor
	@Mutable
	void setHeight(int height);

	@Accessor
	java.util.List<ResourceMetadataSerializer.Value<?>> getAdditionalMetadata();

	@Accessor
	MipmapStrategy getStrategy();

	@Accessor
	float getCutoffBias();

	@Accessor
	@Mutable
	void setAdditionalMetadata(java.util.List<ResourceMetadataSerializer.Value<?>> additionalMetadata);

	@Accessor
	@Mutable
	void setStrategy(MipmapStrategy strategy);

	@Accessor
	@Mutable
	void setCutoffBias(float cutoffBias);
}
