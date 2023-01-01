package dev.notalpha.dashloader.api;

import dev.notalpha.dashloader.cache.CacheManagerFactory;
import dev.notalpha.dashloader.cache.registry.factory.MissingHandler;

import java.util.List;

public interface DashEntrypoint {
	void onDashLoaderInit(CacheManagerFactory factory);

	void onDashLoaderSave(List<MissingHandler<?>> handlers);
}
