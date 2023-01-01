package dev.notalpha.dashloader.api;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryFactory;
import dev.quantumfusion.taski.builtin.StepTask;

public interface DashCacheHandler<M> {
	void reset(DashLoader.Status status);
	M saveMappings(RegistryFactory writer, StepTask task);
	void loadMappings(M mappings, RegistryReader reader, StepTask task);

	Class<M> getDataClass();
	boolean isActive();
	default float taskWeight() {
		return 100;
	}
}
