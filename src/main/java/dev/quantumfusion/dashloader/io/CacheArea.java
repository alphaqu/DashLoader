package dev.quantumfusion.dashloader.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CacheArea {
	public final List<dev.quantumfusion.dashloader.io.SubCacheArea> subCaches;
	public final String name;

	public CacheArea(List<dev.quantumfusion.dashloader.io.SubCacheArea> subCaches, String name) {
		this.subCaches = subCaches;
		this.name = name;
	}

	public Path getPath(final Path cacheDir, final dev.quantumfusion.dashloader.io.SubCacheArea subCacheArea) {
		return cacheDir.resolve(this.name + "/" + subCacheArea.name + "/");
	}

	public void clear(final Path cacheDir) {
		for (dev.quantumfusion.dashloader.io.SubCacheArea subCache : this.subCaches) {
			final Path path = this.getPath(cacheDir, subCache);
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			Files.deleteIfExists(cacheDir.resolve(this.name + "/"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.name).append('\n');

		for (SubCacheArea subCache : this.subCaches) {
			sb.append("\t").append(subCache.name).append(" | ").append(subCache.used).append('\n');
		}

		return sb.toString();
	}
}
