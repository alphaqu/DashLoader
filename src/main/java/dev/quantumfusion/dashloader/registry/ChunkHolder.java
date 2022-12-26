package dev.quantumfusion.dashloader.registry;

import dev.quantumfusion.dashloader.registry.data.ChunkData;

public interface ChunkHolder {
	ChunkData<?, ?>[] getChunks();
}
