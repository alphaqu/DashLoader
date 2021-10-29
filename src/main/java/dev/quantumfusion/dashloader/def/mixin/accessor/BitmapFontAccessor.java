package dev.quantumfusion.dashloader.def.mixin.accessor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.font.BitmapFont;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BitmapFont.class)
public interface BitmapFontAccessor {

	@Invoker("<init>")
	static BitmapFont init(NativeImage image, Int2ObjectMap<BitmapFont.BitmapFontGlyph> glyphs) {
		throw new AssertionError();
	}

	@Accessor
	Int2ObjectMap<BitmapFont.BitmapFontGlyph> getGlyphs();

	@Accessor
	NativeImage getImage();

}
