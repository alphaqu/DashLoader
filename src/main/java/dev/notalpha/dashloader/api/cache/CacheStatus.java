package dev.notalpha.dashloader.api.cache;

public enum CacheStatus {
	/**
	 * The cache manager is doing nothing.
	 */
	IDLE,
	/**
	 * The cache manager is in the process of loading a cache.
	 */
	LOAD,
	/**
	 * The cache manager is creating a cache.
	 */
	SAVE,
}
