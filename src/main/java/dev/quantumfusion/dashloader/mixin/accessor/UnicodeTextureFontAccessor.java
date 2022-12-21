package dev.quantumfusion.dashloader.mixin.accessor;

import net.minecraft.client.font.UnicodeTextureFont;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UnicodeTextureFont.class)
public interface UnicodeTextureFontAccessor {

	@Accessor
	byte[] getSizes();

	@Accessor
	UnicodeTextureFont.FontImage[] getFontImages();

	@Mutable
	@Accessor
	void setSizes(byte[] sizes);

	@Accessor
	@Mutable
	void setFontImages(UnicodeTextureFont.FontImage[] fontImages);
}
