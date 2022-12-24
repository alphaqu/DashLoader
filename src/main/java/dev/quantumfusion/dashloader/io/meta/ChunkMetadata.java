package dev.quantumfusion.dashloader.io.meta;

import java.util.List;

public final class ChunkMetadata {
	public final byte chunkId;
	public final String name;
	public final int dashObjectId;
	public final int size;
	public final long fileSize;
	public final List<FragmentMetadata> fragments;

	public ChunkMetadata(byte chunkId, String name, int dashObjectId, int size, long fileSize, List<FragmentMetadata> fragments) {
		this.chunkId = chunkId;
		this.name = name;
		this.dashObjectId = dashObjectId;
		this.size = size;
		this.fileSize = fileSize;
		this.fragments = fragments;
	}
}
