package dev.notalpha.dashloader.mixin.accessor;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BitmapProvider.Glyph.class)
public interface BitmapFontGlyphAccessor {
	@Accessor
	NativeImage getImage();

	@Accessor("offsetX")
	int getX();

	@Accessor("offsetY")
	int getY();

	@Accessor
	float getScale();

	@Accessor
	int getWidth();

	@Accessor
	int getHeight();

	@Accessor
	int getAdvance();

	@Accessor
	int getAscent();
}
