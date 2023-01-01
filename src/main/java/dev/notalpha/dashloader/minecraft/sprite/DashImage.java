package dev.notalpha.dashloader.minecraft.sprite;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.Dashable;
import dev.notalpha.dashloader.cache.io.def.DataUnsafeByteBuffer;
import dev.notalpha.dashloader.cache.registry.RegistryReader;
import dev.notalpha.dashloader.mixin.accessor.NativeImageAccessor;
import net.minecraft.client.texture.NativeImage;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

@DashObject(NativeImage.class)
public final class DashImage implements Dashable<NativeImage> {
	@DataUnsafeByteBuffer
	public final ByteBuffer image;
	public final NativeImage.Format format;
	public final boolean useSTB;
	public final int width;
	public final int height;

	public DashImage(NativeImage nativeImage) {
		NativeImageAccessor nativeImageAccess = (NativeImageAccessor) (Object) nativeImage;
		this.format = nativeImage.getFormat();
		this.width = nativeImage.getWidth();
		this.height = nativeImage.getHeight();

		final int capacity = this.width * this.height * this.format.getChannelCount();
		final long pointer = nativeImageAccess.getPointer();

		ByteBuffer image1 = MemoryUtil.memByteBuffer(pointer, capacity);
		image1.limit(capacity);
		this.image = image1;
		this.useSTB = nativeImageAccess.getIsStbImage();
	}

	public DashImage(ByteBuffer image, NativeImage.Format format, boolean useSTB, int width, int height) {
		this.image = image;
		this.format = format;
		this.useSTB = useSTB;
		this.width = width;
		this.height = height;
	}

	/**
	 * <h2>I can bet that next dashloader version will change this again. This method needs some serious over engineering.</h2>
	 *
	 * @param registry da registry
	 * @return da image
	 */
	@Override
	public NativeImage export(final RegistryReader registry) {
		image.rewind();
		long pointer = MemoryUtil.memAddress(image);
		return NativeImageAccessor.init(this.format, this.width, this.height, this.useSTB, pointer);
	}
}
