package dev.notalpha.dashloader.io;

import com.github.luben.zstd.Zstd;
import dev.notalpha.taski.builtin.StepTask;
import dev.notalpha.hyphen.io.ByteBufferIO;
import dev.notalpha.hyphen.io.UnsafeIO;
import org.apache.commons.io.IOUtils;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class IOHelper {

	public static int[] toArray(IntBuffer buffer) {
		if (buffer == null) {
			return null;
		}
		buffer.rewind();
		int[] foo = new int[buffer.remaining()];
		buffer.get(foo);
		return foo;
	}

	public static float[] toArray(FloatBuffer buffer) {
		if (buffer == null) {
			return null;
		}

		buffer.rewind();
		float[] foo = new float[buffer.remaining()];
		buffer.get(foo);
		return foo;
	}

	public static IntBuffer fromArray(int[] arr) {
		if (arr == null) {
			return null;
		}

		var buffer = MemoryUtil.memAllocInt(arr.length);
		buffer.put(arr);
		buffer.rewind();
		return buffer;
	}

	public static FloatBuffer fromArray(float[] arr) {
		if (arr == null) {
			return null;
		}

		var buffer = MemoryUtil.memAllocFloat(arr.length);
		buffer.put(arr);
		buffer.rewind();
		return buffer;
	}

	public static void save(Path path, StepTask task, UnsafeIO io, int fileSize, byte compressionLevel) throws IOException {
		io.rewind();
		try (FileChannel channel = createFile(path)) {
			if (compressionLevel > 0) {
				task.reset(4);
				// Allocate
				final long maxSize = Zstd.compressBound(fileSize);
				final var dst = UnsafeIO.create((int) maxSize);
				task.next();

				// Compress
				final long size = Zstd.compressUnsafe(dst.address(), maxSize, io.address(), fileSize, compressionLevel);
				task.next();

				// Write
				dst.rewind();
				final var map = channel.map(FileChannel.MapMode.READ_WRITE, 0, size + 5).order(ByteOrder.LITTLE_ENDIAN);
				task.next();

				map.put(compressionLevel);
				map.putInt(fileSize);
				ByteBuffer byteBuffer = MemoryUtil.memByteBufferSafe(dst.address(), (int) maxSize);
				map.put(byteBuffer);
				io.close();
				dst.close();
			} else {
				task.reset(2);
				final var map = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize + 1).order(ByteOrder.LITTLE_ENDIAN);
				task.next();
				ByteBufferIO file = ByteBufferIO.wrap(map);
				file.putByte(compressionLevel);
				ByteBuffer byteBuffer = MemoryUtil.memByteBufferSafe(io.address(), fileSize);
				file.putByteBuffer(byteBuffer, fileSize);
				task.next();
			}
		}
	}

	public static UnsafeIO load(Path path) throws IOException {
		try (FileChannel channel = openFile(path)) {
			long fileSize = channel.size();
			var buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize).order(ByteOrder.LITTLE_ENDIAN);
			// Check compression
			if (buffer.get() > 0) {
				final int size = buffer.getInt();
				final var dst = UnsafeIO.create(size);
				Zstd.decompressUnsafe(dst.address(), size, MemoryUtil.memAddress(buffer), fileSize);
				dst.rewind();
				return dst;
			} else {
				return UnsafeIO.wrap(buffer);
			}
		}
	}

	public static FileChannel createFile(Path path) throws IOException {
		Files.createDirectories(path.getParent());
		Files.deleteIfExists(path);
		return FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
	}

	public static FileChannel openFile(Path path) throws IOException {
		return FileChannel.open(path, StandardOpenOption.READ);
	}

	public static byte[] streamToArray(InputStream inputStream) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream() {
			@Override
			public synchronized byte[] toByteArray() {
				return this.buf;
			}
		};
		IOUtils.copy(inputStream, output);
		return output.toByteArray();
	}
}
