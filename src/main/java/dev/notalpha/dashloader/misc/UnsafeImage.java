package dev.notalpha.dashloader.misc;

import com.mojang.blaze3d.platform.NativeImage;
import dev.notalpha.dashloader.mixin.accessor.NativeImageAccessor;
import org.lwjgl.system.MemoryUtil;

public final class UnsafeImage {
	public final NativeImage image;
	public final int width;
	public final int height;
	public final long pointer;

	public UnsafeImage(NativeImage image) {
		this.image = image;
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.pointer = ((NativeImageAccessor) (Object) image).getPixels();
	}

	public int get(int x, int y) {
		return MemoryUtil.memGetInt(this.pointer + ((long) x + (long) y * (long) width) * 4L);
	}

	public void set(int x, int y, int value) {
		MemoryUtil.memPutInt(this.pointer + ((long) x + (long) y * (long) width) * 4L, value);
	}
}
