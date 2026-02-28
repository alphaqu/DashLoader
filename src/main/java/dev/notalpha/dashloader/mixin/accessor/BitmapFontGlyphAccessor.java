package dev.notalpha.dashloader.mixin.accessor;

import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.client.gui.font.providers.BitmapProvider$Glyph")
public interface BitmapFontGlyphAccessor {
@Invoker("<init>")
static Object init(float scaleFactor, NativeImage image, int x, int y, int width, int height, int advance, int ascent) {
throw new AssertionError();
}

@Accessor
NativeImage getImage();

@Accessor("offsetX")
int getX();

@Accessor("offsetY")
int getY();

@Accessor("scale")
float getScaleFactor();

@Accessor
int getWidth();

@Accessor
int getHeight();

@Accessor
int getAdvance();

@Accessor
int getAscent();
}
