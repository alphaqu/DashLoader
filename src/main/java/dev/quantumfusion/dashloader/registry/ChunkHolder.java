package dev.quantumfusion.dashloader.registry;

import dev.quantumfusion.dashloader.registry.chunk.data.AbstractDataChunk;

public interface ChunkHolder {
	AbstractDataChunk<?, ?>[] getChunks();
}
