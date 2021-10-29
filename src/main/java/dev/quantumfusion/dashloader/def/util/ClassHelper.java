package dev.quantumfusion.dashloader.def.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClassHelper {

	public static final Map<String, Class<?>> cache = new ConcurrentHashMap<>();


	@SuppressWarnings("unchecked")
	public static <T> Class<T> castClass(Class<?> aClass) {
		return (Class<T>) aClass;
	}


	public static Class<?> forName(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Class<?> getClass(final String className) {
		final Class<?> closs = cache.get(className);
		if (closs != null) return closs;
		try {
			final Class<?> clz = Class.forName(className);
			cache.put(className, clz);
			return clz;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	public static String printClasses(Class<?>[] classes) {
		StringBuilder builder = new StringBuilder();
		for (Class<?> aClass : classes) builder.append(aClass.getSimpleName());
		return builder.toString();
	}

	public static String printClasses(List<Class<?>> classes) {
		StringBuilder builder = new StringBuilder();
		for (Class<?> aClass : classes) builder.append(aClass.getSimpleName());
		return builder.toString();
	}
}