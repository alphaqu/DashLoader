package dev.quantumfusion.dashloader.api;

import dev.quantumfusion.dashloader.registry.RegistryFactory;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.taski.builtin.StepTask;

public interface DashCacheHandler<M> {
	void prepareForSave();
	M saveMappings(RegistryFactory writer, StepTask task);
	void loadMappings(M mappings, RegistryReader reader, StepTask task);

	Class<M> getDataClass();
	boolean isActive();
	default float taskWeight() {
		return 100;
	}
}
