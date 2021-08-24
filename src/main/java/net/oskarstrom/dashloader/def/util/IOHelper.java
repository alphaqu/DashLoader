package net.oskarstrom.dashloader.def.util;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.stream.IntStream;

public class IOHelper {

	public static int[] toArray(IntBuffer buffer, int size) {
		int[] bufferOut = new int[size];
		buffer.get(bufferOut);
		return bufferOut;
	}

	public static float[] toArray(FloatBuffer buffer, int size) {
		float[] bufferOut = new float[size];
		buffer.get(bufferOut);
		return bufferOut;
	}


	public static byte[] toArray(ByteBuffer buffer, int size) {
		byte[] bufferOut = new byte[size];
		buffer.get(bufferOut);
		return bufferOut;
	}

	public static Integer[] toBoxedArray(IntBuffer buffer, int size) {
		int[] integers = toArray(buffer, size);
		Integer[] out = new Integer[integers.length];

		for (int i = 0; i < integers.length; i++) {
			out[i] = integers[i];
		}

		return out;
	}

	public static Float[] toBoxedArray(FloatBuffer buffer, int size) {
		float[] floats = toArray(buffer, size);
		Float[] out = new Float[floats.length];

		for (int i = 0; i < floats.length; i++) {
			out[i] = floats[i];
		}

		return out;
	}





	public static byte[] streamToArray(InputStream inputStream) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream() {
			@Override
			public synchronized byte[] toByteArray() {
				return buf;
			}
		};
		IOUtils.copy(inputStream, output);
		return output.toByteArray();
	}
}
