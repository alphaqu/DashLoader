package net.oskarstrom.dashloader.def;

import io.activej.serializer.SerializerBuilder;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtils {
	public static void testCreation(Class<?>... oClass) {
		for (Class<?> aClass : oClass) {
			testCreation(aClass);
		}
	}

	public static void testCreation(Class<?> clazz) {
		try {
			SerializerBuilder.create().build(clazz);
		} catch (RuntimeException e) {
			System.out.println(clazz);
			e.printStackTrace();
			fail();
		}
	}


	public static void main(String[] args) {

		final int _x = 0xFFFFFFFF;
		final String x = Integer.toString(_x >>> 4,2);
		for (int i = 0; i < x.toCharArray().length; i++) {
			System.out.print(x.charAt(i));
			if (i % 4 == 3) {
				System.out.print(' ');
			}
		}
		System.out.println();
		parse(_x);


	}


	public static void parse(int integer) {
		System.out.println(integer >>> 4);
		System.out.println((byte) (integer << 24));
	}
}
