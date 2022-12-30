package dev.quantumfusion.dashloader.io.data;


import dev.quantumfusion.dashloader.io.data.fragment.CacheFragment;

import java.util.List;

public class CacheInfo {
	/**
	 * Information about the different file fragments the cache contains.
	 */
	public final List<CacheFragment> fragments;
	/**
	 * Information about the output chunks.
	 */
	public final List<ChunkInfo> chunks;

	/**
	 * A two dimensional array containing the sizes of the stages and chunks.
	 * The first index is the stage index which will yield an array of the chunk sizes,
	 * The size of this array is the amount of chunks in that stage.
	 */
	public final int[][] stageSizes;
	/**
	 * The unix time of the original cache creation time.
	 */
	public long timeCreated;

	/**
	 * The unix time of the last cache loading use.
	 */
	public long timeLastLoaded;

	/**
	 * The amount of times the cache has been loaded.
	 */
	public long countLoaded;

	public CacheInfo(List<CacheFragment> fragments, List<ChunkInfo> chunks, int[][] stageSizes, long timeCreated, long timeLastLoaded, long countLoaded) {
		this.fragments = fragments;
		this.chunks = chunks;
		this.stageSizes = stageSizes;
		this.timeCreated = timeCreated;
		this.timeLastLoaded = timeLastLoaded;
		this.countLoaded = countLoaded;
	}
}
