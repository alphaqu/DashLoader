package net.oskarstrom.dashloader.def.image;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.texture.NativeImage;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.def.mixin.accessor.NativeImageAccessor;
import net.oskarstrom.dashloader.def.util.IOHelper;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Data
public class DashImage implements Dashable<NativeImage> {
	public final byte[] image;
	public final NativeImage.Format format;
	public final boolean useSTB;
	public final int width;
	public final int height;


	public DashImage(NativeImage nativeImage) {
		try {
			NativeImageAccessor nativeImageAccess = (NativeImageAccessor) (Object) nativeImage;
			this.format = nativeImage.getFormat();
			this.width = nativeImage.getWidth();
			this.height = nativeImage.getHeight();
			this.image = write(nativeImageAccess.getPointer());
			this.useSTB = nativeImageAccess.getIsStbImage();
		} catch (IOException e) {
			throw new RuntimeException("Failed to create image. Reason: ", e);
		}
	}

	public DashImage(byte[] image,
					 NativeImage.Format format,
					 boolean useSTB,
					 int width,
					 int height) {
		this.image = image;
		this.format = format;
		this.useSTB = useSTB;
		this.width = width;
		this.height = height;
	}


	private byte[] write(long pointer) throws IOException {
		final int channelCount = this.format.getChannelCount();
		final GLCallback writeCallback = new GLCallback();
		try {
			if (STBImageWrite.nstbi_write_png_to_func(writeCallback.address(), 0L, width, height, channelCount, pointer, 0) != 0) {
				return writeCallback.getBytes();
			}
		} finally {
			writeCallback.free();
		}
		throw new RuntimeException("Failed to serialize image. Reason: " + STBImage.stbi_failure_reason());
	}

	/**
	 * <h2>I can bet that next dashloader version will change this again. This method needs some serious over engineering.</h2>
	 *
	 * @param registry da registry
	 * @return da image
	 */
	@Override
	public final NativeImage toUndash(final DashExportHandler registry) {
		final ByteBuffer buf = ByteBuffer.allocateDirect(image.length);
		buf.put(image);
		buf.flip();
		ByteBuffer buffer = STBImage.stbi_load_from_memory(
				buf,
				new int[1],
				new int[1],
				new int[1],
				format.getChannelCount());
		if (buffer == null) {
			throw new RuntimeException("Could not load image: " + STBImage.stbi_failure_reason());
		}
		return NativeImageAccessor.init(format, this.width, this.height, useSTB, MemoryUtil.memAddress(buffer));
	}

	private static class GLCallback extends STBIWriteCallback {
		private final ByteArrayOutputStream output;


		private GLCallback() {
			this.output = new ByteArrayOutputStream();
		}

		public void invoke(long context, long data, int size) {
			try {
				output.write(IOHelper.toArray(STBIWriteCallback.getData(data, size), size));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public byte[] getBytes() {
			return output.toByteArray();
		}


	}


}
