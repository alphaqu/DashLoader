package dev.notalpha.dashloader.api;

import dev.notalpha.dashloader.cache.CacheManager;
import dev.notalpha.dashloader.cache.registry.RegistryFactory;
import dev.notalpha.dashloader.cache.registry.RegistryReader;
import dev.quantumfusion.taski.builtin.StepTask;

public interface DashCacheHandler<M> {
	void reset(CacheManager cacheManager);

	M saveMappings(RegistryFactory writer, StepTask task);

	void loadMappings(M mappings, RegistryReader reader, StepTask task);

	Class<M> getDataClass();

	boolean isActive();

	default float taskWeight() {
		return 100;
	}
}
