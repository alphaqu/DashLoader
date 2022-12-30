package dev.quantumfusion.dashloader.io.data;

import dev.quantumfusion.dashloader.registry.data.ChunkFactory;

public class ChunkInfo {
	public final int dashObjectId;
	public final int size;
	public final String name;

	public ChunkInfo(int dashObjectId, int size, String name) {
		this.dashObjectId = dashObjectId;
		this.size = size;
		this.name = name;
	}

	public ChunkInfo(ChunkFactory<?, ?> chunk) {
		this.dashObjectId = chunk.dashObject.getDashObjectId();
		this.size = chunk.list.size();
		this.name = chunk.name;
	}
}
