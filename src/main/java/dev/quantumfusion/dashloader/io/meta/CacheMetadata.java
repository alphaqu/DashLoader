package dev.quantumfusion.dashloader.io.meta;


import java.util.List;

public class CacheMetadata {
	public final ChunkMetadata[] chunks;
	public final long timeCreated;
	public final long timeLastLoaded;
	public final long countLoaded;
	public final int compressionLevel;

	public CacheMetadata(ChunkMetadata[] chunks, long timeCreated, long timeLastLoaded, long countLoaded, int compressionLevel) {
		this.chunks = chunks;
		this.timeCreated = timeCreated;
		this.timeLastLoaded = timeLastLoaded;
		this.countLoaded = countLoaded;
		this.compressionLevel = compressionLevel;
	}
}
