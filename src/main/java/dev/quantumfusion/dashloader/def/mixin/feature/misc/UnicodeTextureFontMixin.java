package dev.quantumfusion.dashloader.def.mixin.feature.misc;

import net.minecraft.client.font.UnicodeTextureFont;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UnicodeTextureFont.class)
public class UnicodeTextureFontMixin {

	@Inject(
			method = "getImageId(I)Lnet/minecraft/util/Identifier;",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void fastGetId(int codePoint, CallbackInfoReturnable<Identifier> cir) {
		final String id = Integer.toHexString((codePoint & (~0xFF)) >> 8);
		cir.setReturnValue(new Identifier("textures/font/unicode_page_" + (id.length() == 1 ? 0 + id : id) + ".png"));
	}


}
