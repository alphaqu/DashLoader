package dev.notalpha.dashloader.api.cache;

import java.nio.file.Path;

public interface DashCache {
	CacheStatus getStatus();
	Path getDir();
}
