package dev.quantumfusion.dashloader.registry;

import dev.quantumfusion.dashloader.registry.chunk.data.DataChunk;

public interface ChunkHolder {
	DataChunk<?, ?>[] getChunks();
}
