package dev.quantumfusion.dashloader.api.hook;

import dev.quantumfusion.dashloader.DashDataManager;
import dev.quantumfusion.dashloader.data.MappingData;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.taski.builtin.StepTask;

public interface LoadCacheHook {
	default void loadCacheStart() {
	}

	default void loadCacheTask(StepTask task) {
	}

	default void loadCacheDeserialization() {
	}

	default void loadCacheRegistryInit(RegistryReader reader, DashDataManager dashDataManager, MappingData mappingData) {
	}

	default void loadCacheExported(RegistryReader reader, DashDataManager dashDataManager, MappingData mappingData) {
	}

	default void loadCacheMapped(RegistryReader reader, DashDataManager dashDataManager, MappingData mappingData) {
	}

	default void loadCacheEnd() {
	}
}
