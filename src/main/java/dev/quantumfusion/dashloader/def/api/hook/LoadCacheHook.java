package dev.quantumfusion.dashloader.def.api.hook;

import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.corehook.MappingData;
import dev.quantumfusion.taski.builtin.StepTask;

public interface LoadCacheHook {
	default void loadCacheStart() {}
	default void loadCacheTask(StepTask task) {}
	default void loadCacheDeserialization() {}
	default void loadCacheRegistryInit(RegistryReader reader, DashDataManager dashDataManager, MappingData mappingData) {}
	default void loadCacheExported(RegistryReader reader, DashDataManager dashDataManager, MappingData mappingData) {}
	default void loadCacheMapped(RegistryReader reader, DashDataManager dashDataManager, MappingData mappingData) {}
	default void loadCacheEnd() {}
}
