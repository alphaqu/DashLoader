package dev.notalpha.dashloader.api.cache;

import dev.notalpha.dashloader.CacheFactoryImpl;
import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.DashObject;

import java.nio.file.Path;

/**
 * The CacheFactory is used to construct a {@link Cache}
 */
public interface CacheFactory {
	/**
	 * Creates a new Factory
	 *
	 * @return CacheFactory
	 */
	static CacheFactory create() {
		return new CacheFactoryImpl();
	}

	/**
	 * Adds a DashObject to the Cache, this will allow the Cache to cache the DashObject's target.
	 *
	 * @param dashClass The class
	 */
	void addDashObject(Class<? extends DashObject<?>> dashClass);

	/**
	 * Adds a module to the Cache. Please note only enabled Modules will actually be cached.
	 */
	void addModule(DashModule<?> module);

	/**
	 * Builds the cache object.
	 *
	 * @param path The directory which contains the caches.
	 * @return A DashLoader cache object.
	 */
	Cache build(Path path);
}
