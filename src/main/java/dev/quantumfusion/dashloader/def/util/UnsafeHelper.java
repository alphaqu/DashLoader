package dev.quantumfusion.dashloader.def.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class UnsafeHelper {

	private static final MethodHandle allocateInstance;

	static {
		try {
			final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
			allocateInstance = MethodHandles.lookup().findVirtual(unsafeClass, "allocateInstance", MethodType.methodType(Object.class, Class.class)).bindTo(getUnsafe(unsafeClass));
		} catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException e) {
			throw new RuntimeException("Unable to bind AllocateInstance.");
		}
	}

	@SuppressWarnings("unchecked")
	public static <O> O allocateInstance(Class<O> closs) {
		try {
			return (O) allocateInstance.invokeExact(closs);
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	private static Object getUnsafe(Class<?> unsafeClass) throws ClassNotFoundException, IllegalAccessException {
		final int mods = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;
		for (Field field : unsafeClass.getDeclaredFields()) {
			if (field.getModifiers() == mods && field.getType() == unsafeClass) {
				field.setAccessible(true);
				final Object possibleUnsafe = field.get(null);
				if (possibleUnsafe != null) {
					return possibleUnsafe;
				}
			}
		}

		throw new RuntimeException("Unable to find Sun UnsafeHelper Library.");
	}
}
