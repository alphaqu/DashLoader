package dev.notalpha.dashloader.api;

import dev.notalpha.dashloader.Cache;
import dev.notalpha.dashloader.registry.RegistryFactory;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.quantumfusion.taski.builtin.StepTask;

public interface DashModule<M> {
	void reset(Cache cacheManager);

	M save(RegistryFactory writer, StepTask task);

	void load(M mappings, RegistryReader reader, StepTask task);

	Class<M> getDataClass();

	boolean isActive();

	default float taskWeight() {
		return 100;
	}
}
