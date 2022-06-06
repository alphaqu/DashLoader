package dev.quantumfusion.dashloader.mixin.option.misc;

import net.minecraft.client.font.UnicodeTextureFont;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(UnicodeTextureFont.class)
public class UnicodeTextureFontMixin {

	@Overwrite
	private Identifier getImageId(int codePoint) {
		final String id = Integer.toHexString((codePoint & (~0xFF)) >> 8);
		return new Identifier("textures/font/unicode_page_" + (id.length() == 1 ? 0 + id : id) + ".png");
	}

}
