package dev.notalpha.dashloader.mixin.accessor;

import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NativeImage.class)
public interface NativeImageAccessor {
	@Invoker("<init>")
	static NativeImage init(NativeImage.Format format, int width, int height, boolean useStb, long pointer) {
		throw new AssertionError();
	}

	@Accessor("pixels")
	long getPointer();

	@Accessor("useStbFree")
	boolean getIsStbImage();
}
