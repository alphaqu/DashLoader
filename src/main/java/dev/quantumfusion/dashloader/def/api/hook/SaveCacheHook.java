package dev.quantumfusion.dashloader.def.api.hook;

import dev.quantumfusion.dashloader.core.registry.ChunkHolder;
import dev.quantumfusion.dashloader.core.registry.RegistryHandler;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.corehook.MappingData;
import dev.quantumfusion.taski.builtin.StepTask;

import java.util.List;

public interface SaveCacheHook {
	default void saveCacheStart() {}
	default void saveCacheTask(StepTask task) {}
	default void saveCacheRegistryInit(RegistryHandler handler) {}
	default void saveCacheRegistryWriterInit(RegistryWriter writer) {}
	default void saveCacheMappingStart(RegistryWriter writer, MappingData data) {}
	default void saveCacheMappingEnd(RegistryWriter writer, MappingData data) {}
	default void saveCachePopulateHolders(RegistryWriter writer, MappingData data, List<ChunkHolder> holders) {}
	default void saveCacheSerialize(RegistryWriter writer, MappingData data, List<ChunkHolder> holders) {}
	default void saveCacheEnd() {}
}
