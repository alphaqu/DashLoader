package dev.notalpha.dashloader.api;


import dev.notalpha.dashloader.api.cache.CacheFactory;

import java.util.List;

/**
 * The DashEntrypoint allows operations on the DashLoader Minecraft cache, like adding support to external DashObjects, Modules or MissingHandlers.
 */
public interface DashEntrypoint {
	/**
	 * Runs on DashLoader initialization. This is quite early compared to the cache.
	 *
	 * @param factory Factory to register your DashObjects/Modules to.
	 */
	void onDashLoaderInit(CacheFactory factory);

	/**
	 * Runs when DashLoader is about to start creating a cache.
	 * This is where you provide missing handlers for dashloader to use when it fails to serialize an object.
	 */
	void onDashLoaderSave(List<MissingHandler<?>> handlers);
}
