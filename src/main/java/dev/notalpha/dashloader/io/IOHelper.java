package dev.notalpha.dashloader.io;

import com.github.luben.zstd.Zstd;
import dev.notalpha.dashloader.misc.UnsafeHelper;
import dev.notalpha.hyphen.io.ByteBufferIO;
import dev.notalpha.taski.builtin.StepTask;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
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

	public static byte[] toArray(ByteBuffer buffer) {
		if (buffer == null) {
			return null;
		}

		buffer.rewind();
		byte[] foo = new byte[buffer.remaining()];
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

	public static void save(Path path, StepTask task, ByteBufferIO io, int fileSize, byte compressionLevel) throws IOException {
		io.rewind();
		io.byteBuffer.limit(fileSize);
		try (FileChannel channel = createFile(path)) {
			ByteBuffer map = null;
			try {
				if (compressionLevel > 0) {
					task.reset(4);
					// Allocate. Owned here and freed in finally, instead of waiting
					// for the GC to clean up a large direct buffer.
					final long maxSize = Zstd.compressBound(fileSize);
					final ByteBuffer dst = MemoryUtil.memAlloc((int) maxSize);
					try {
						task.next();

						// Compress
						final long size = Zstd.compress(dst, io.byteBuffer, compressionLevel);
						task.next();

						// Write
						dst.position(0);
						dst.limit((int) size);
						map = channel.map(FileChannel.MapMode.READ_WRITE, 0, size + 5).order(ByteOrder.LITTLE_ENDIAN);
						task.next();

						map.put(compressionLevel);
						map.putInt(fileSize);
						map.put(dst);
						io.close();
					} finally {
						dst.position(0);
						MemoryUtil.memFree(dst);
					}
				} else {
					task.reset(2);
					map = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize + 1).order(ByteOrder.LITTLE_ENDIAN);
					task.next();
					ByteBufferIO file = ByteBufferIO.wrap(map);
					file.putByte(compressionLevel);
					file.putByteBuffer(io.byteBuffer, fileSize);
					task.next();
				}
			} finally {
				// Closing the channel above is not enough, the mapping outlives it and
				// keeps the file open. Windows then refuses to move or delete the
				// directory, which breaks the atomic cache commit in CacheImpl.save.
				unmap(map);
			}
		}
	}

	public static ByteBufferIO load(Path path) throws IOException {
		try (FileChannel channel = openFile(path)) {
			var buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()).order(ByteOrder.LITTLE_ENDIAN);
			// Check compression
			if (buffer.get() > 0) {
				final int size = buffer.getInt();
				// memAlloc so the caller can free it right after reading via release().
				final ByteBuffer dst = MemoryUtil.memAlloc(size).order(ByteOrder.LITTLE_ENDIAN);
				try {
					Zstd.decompress(dst, buffer);
				} catch (RuntimeException e) {
					MemoryUtil.memFree(dst);
					throw e;
				}
				dst.position(0);
				return ByteBufferIO.wrap(dst);
			} else {
				return ByteBufferIO.wrap(buffer);
			}
		}
	}

	/**
	 * Frees a buffer returned by {@link #load(Path)}.
	 * <p>
	 * Only buffers that {@link #load} allocated itself are freed. Without
	 * compression the buffer is memory mapped and its lifetime belongs to the JVM,
	 * which unmaps it on cleanup - freeing it here would be a double free.
	 * <p>
	 * {@link ByteBufferIO#close()} is deliberately not used for this, it only
	 * clears the buffer and leaks the native memory until the next GC.
	 */
	public static void release(ByteBufferIO io) {
		ByteBuffer buffer = io.byteBuffer;
		if (buffer instanceof MappedByteBuffer) {
			return;
		}
		if (buffer.isDirect()) {
			buffer.position(0);
			MemoryUtil.memFree(buffer);
		}
	}

	/**
	 * Releases a memory mapping created by {@link FileChannel#map}. Does nothing
	 * for buffers that are not mapped.
	 */
	public static void unmap(ByteBuffer buffer) {
		if (buffer instanceof MappedByteBuffer) {
			UnsafeHelper.UNSAFE.invokeCleaner(buffer);
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
			public synchronized byte @NotNull [] toByteArray() {
				return this.buf;
			}
		};
		IOUtils.copy(inputStream, output);
		return output.toByteArray();
	}
}
