package dev.quantumfusion.dashloader.registry;

import dev.quantumfusion.dashloader.registry.chunk.DataChunk;

public interface ChunkHolder {
	DataChunk<?, ?>[] getChunks();
}
