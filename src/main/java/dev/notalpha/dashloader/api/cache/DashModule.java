package dev.notalpha.dashloader.api.cache;

import dev.notalpha.dashloader.api.RegistryReader;
import dev.notalpha.dashloader.api.RegistryWriter;
import dev.quantumfusion.taski.builtin.StepTask;

public interface DashModule<M> {
	void reset(DashCache cacheManager);

	M save(RegistryWriter writer, StepTask task);

	void load(M mappings, RegistryReader reader, StepTask task);

	Class<M> getDataClass();

	default boolean isActive() {
		return true;
	}

	default float taskWeight() {
		return 100;
	}
}
