package dev.notalpha.dashloader.mixin.option.misc;

import com.mojang.blaze3d.platform.NativeImage;
import dev.notalpha.dashloader.mixin.accessor.NativeImageAccessor;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MipmapGenerator.class)
public abstract class MipmapHelperMixin {
	// not using wrapOperation because this is just replacing the call
	@Redirect(
			method = {"hasTransparentPixel", "generateMipLevels"},
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;getPixel(II)I")
	)
	private static int getColor(NativeImage instance, int x, int y) {
		return MemoryUtil.memGetInt(((NativeImageAccessor) (Object) instance).getPixels() + ((long) x + (long) y * (long) instance.getWidth()) * 4L);
	}

	@Redirect(
			method = "generateMipLevels",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;setPixel(III)V")
	)
	private static void setColor(NativeImage instance, int x, int y, int color) {
		MemoryUtil.memPutInt(((NativeImageAccessor) (Object) instance).getPixels() + ((long) x + (long) y * (long) instance.getWidth()) * 4L, color);
	}
}
