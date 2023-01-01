package dev.notalpha.dashloader.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClassHelper {
	public static final Map<String, Class<?>> cache = new ConcurrentHashMap<>();

	public static Class<?> getClass(final String className) {
		final Class<?> closs = cache.get(className);
		if (closs != null) {
			return closs;
		}
		try {
			final Class<?> clz = Class.forName(className);
			cache.put(className, clz);
			return clz;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}