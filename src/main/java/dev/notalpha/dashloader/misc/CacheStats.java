package dev.notalpha.dashloader.misc;

import dev.notalpha.dashloader.DashLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class CacheStats {
	private static final String STATS_FILE_NAME = "stats.json";
	private static final Pattern VANILLA_TIME = Pattern.compile("vanillaMs\\s*=\\s*(-?\\d+)");

	private static volatile long cachedMs = -1;
	private static volatile long vanillaMs = -1;
	private static volatile long cacheBytes = -1;
	private static volatile int fragments = -1;

	private CacheStats() {
	}

	public static void recordLoad(long cachedMs, long vanillaMs, long cacheBytes, int fragments) {
		CacheStats.cachedMs = cachedMs;
		CacheStats.vanillaMs = vanillaMs;
		CacheStats.cacheBytes = cacheBytes;
		CacheStats.fragments = fragments;
	}

	public static void clear() {
		cachedMs = -1;
		vanillaMs = -1;
		cacheBytes = -1;
		fragments = -1;
	}

	public static boolean hasStats() {
		return cachedMs > 0;
	}

	public static String format() {
		StringBuilder out = new StringBuilder("DashLoader: load in ");
		out.append(ProfilerUtil.getTimeString(cachedMs));
		if (vanillaMs > 0) {
			out.append(" (vanilla ").append(ProfilerUtil.getTimeString(vanillaMs)).append(')');
		}
		if (cacheBytes > 0) {
			out.append(" · ").append(formatBytes(cacheBytes));
		}
		if (fragments > 0) {
			out.append(" · ").append(fragments).append(fragments == 1 ? " fragment" : " fragments");
		}
		return out.toString();
	}

	public static String formatBytes(long bytes) {
		if (bytes >= 1024L * 1024L * 1024L) {
			return String.format(Locale.ROOT, "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
		}
		if (bytes >= 1024L * 1024L) {
			return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
		}
		if (bytes >= 1024L) {
			return String.format(Locale.ROOT, "%.1f kB", bytes / 1024.0);
		}
		return bytes + " B";
	}

	public static long directorySize(Path dir) {
		if (dir == null || !Files.isDirectory(dir)) {
			return -1;
		}
		try (Stream<Path> stream = Files.walk(dir)) {
			return stream.filter(Files::isRegularFile).mapToLong(path -> {
				try {
					return Files.size(path);
				} catch (IOException e) {
					return 0L;
				}
			}).sum();
		} catch (IOException e) {
			return -1;
		}
	}

	public static void writeVanillaTiming(Path cacheDir, long millis) {
		if (cacheDir == null) {
			return;
		}
		try {
			Files.writeString(cacheDir.resolve(STATS_FILE_NAME),
					"vanillaMs=" + millis + "\n",
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING,
					StandardOpenOption.WRITE);
		} catch (IOException e) {
			DashLoader.LOG.warn("Could not write cache stats", e);
		}
	}

	public static long readVanillaTiming(Path cacheDir) {
		if (cacheDir == null) {
			return -1;
		}
		Path path = cacheDir.resolve(STATS_FILE_NAME);
		if (!Files.isRegularFile(path)) {
			return -1;
		}
		try {
			Matcher matcher = VANILLA_TIME.matcher(Files.readString(path, StandardCharsets.UTF_8));
			return matcher.find() ? Long.parseLong(matcher.group(1)) : -1;
		} catch (IOException | RuntimeException e) {
			return -1;
		}
	}
}