package dev.notalpha.dashloader.mixin.option.misc;

import dev.notalpha.dashloader.mixin.accessor.NativeImageAccessor;
import net.minecraft.client.texture.MipmapHelper;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.math.ColorHelper;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MipmapHelper.class)
public abstract class MipmapHelperMixin {
	// not using wrapOperation because this is just replacing the call
	@Redirect(
		method = {"hasAlpha", "getMipmapLevelsImages"},
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;getColorArgb(II)I")
	)
	private static int getColor(NativeImage instance, int x, int y) {
		long pixel = ((NativeImageAccessor) (Object) instance).getPointer()
				+ ((long) x + (long) y * (long) instance.getWidth()) * 4L;
		return ColorHelper.fromAbgr(MemoryUtil.memGetInt(pixel));
	}

	@Redirect(
		method = "getMipmapLevelsImages",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;setColorArgb(III)V")
	)
	private static void setColor(NativeImage instance, int x, int y, int color) {
		long pixel = ((NativeImageAccessor) (Object) instance).getPointer()
				+ ((long) x + (long) y * (long) instance.getWidth()) * 4L;
		MemoryUtil.memPutInt(pixel, ColorHelper.toAbgr(color));
	}
}
