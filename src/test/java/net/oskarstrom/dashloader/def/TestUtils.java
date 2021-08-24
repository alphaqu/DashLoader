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
}
