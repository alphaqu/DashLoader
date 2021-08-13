package net.oskarstrom.dashloader.def.mixin.accessor;

import net.minecraft.client.font.UnicodeTextureFont;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(UnicodeTextureFont.class)
public interface UnicodeTextureFontAccessor {

	@Accessor
	byte[] getSizes();

	@Mutable
	@Accessor
	void setSizes(byte[] sizes);

	@Accessor
	String getTemplate();

	@Accessor
	@Mutable
	void setTemplate(String template);

	@Accessor
	Map<Identifier, NativeImage> getImages();

	@Accessor
	@Mutable
	void setImages(Map<Identifier, NativeImage> images);

	@Accessor
	@Mutable
	void setResourceManager(ResourceManager resourceManager);
}
