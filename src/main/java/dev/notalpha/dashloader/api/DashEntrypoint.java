package dev.notalpha.dashloader.api;

import dev.notalpha.dashloader.CacheFactory;

import java.util.List;

public interface DashEntrypoint {
	void onDashLoaderInit(CacheFactory factory);

	void onDashLoaderSave(List<MissingHandler<?>> handlers);
}
