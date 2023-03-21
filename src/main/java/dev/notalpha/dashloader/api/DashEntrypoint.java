package dev.notalpha.dashloader.api;

import dev.notalpha.dashloader.api.cache.DashCacheFactory;

public interface DashEntrypoint {
	void onDashLoaderInit(DashCacheFactory factory);
}
