package dev.quantumfusion.dashloader.minecraft.sprite;

import dev.quantumfusion.dashloader.api.Dashable;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.io.serializer.DataUnsafeByteBuffer;
import dev.quantumfusion.dashloader.mixin.accessor.NativeImageAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
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


	private byte[] write(long pointer) {
		final int capacity = this.width * this.height * this.format.getChannelCount();
		final var byteBuffer = MemoryUtil.memByteBuffer(pointer, capacity);
		final byte[] bytes = new byte[capacity];
		byteBuffer.get(bytes);
		return bytes;
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
