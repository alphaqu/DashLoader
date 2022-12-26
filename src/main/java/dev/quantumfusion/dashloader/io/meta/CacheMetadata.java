package dev.quantumfusion.dashloader.io.meta;


import java.util.List;

public class CacheMetadata {
	public final List<FragmentMetadata> fragments;
	public final int[][] stageSizes;
	public final List<ChunkInfo> chunks;
	public final long timeCreated;
	public final long timeLastLoaded;
	public final long countLoaded;
	public final int compressionLevel;

	public CacheMetadata(List<FragmentMetadata> fragments, int[][] stageSizes, List<ChunkInfo> chunks, long timeCreated, long timeLastLoaded, long countLoaded, int compressionLevel) {
		this.fragments = fragments;
		this.stageSizes = stageSizes;
		this.chunks = chunks;
		this.timeCreated = timeCreated;
		this.timeLastLoaded = timeLastLoaded;
		this.countLoaded = countLoaded;
		this.compressionLevel = compressionLevel;
	}
}
