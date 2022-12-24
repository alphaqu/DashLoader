package dev.quantumfusion.dashloader.util;

import org.apache.commons.io.IOUtils;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class IOHelper {

	public static int[] toArray(IntBuffer buffer) {
		buffer.rewind();
		int[] foo = new int[buffer.remaining()];
		buffer.get(foo);
		return foo;
	}

	public static float[] toArray(FloatBuffer buffer) {
		buffer.rewind();
		float[] foo = new float[buffer.remaining()];
		buffer.get(foo);
		return foo;
	}

	public static IntBuffer fromArray(int[] arr) {
		var buffer = MemoryUtil.memAllocInt(arr.length);
		buffer.put(arr);
		buffer.rewind();
		return buffer;
	}

	public static FloatBuffer fromArray(float[] arr) {
		var buffer = MemoryUtil.memAllocFloat(arr.length);
		buffer.put(arr);
		buffer.rewind();
		return buffer;
	}

	public static FileChannel createFile(Path path) throws IOException {
		Files.createDirectories(path.getParent());
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
