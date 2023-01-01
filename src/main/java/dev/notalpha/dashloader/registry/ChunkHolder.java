package dev.notalpha.dashloader.registry;

import dev.notalpha.dashloader.registry.data.ChunkData;

public interface ChunkHolder {
	ChunkData<?, ?>[] getChunks();
}
