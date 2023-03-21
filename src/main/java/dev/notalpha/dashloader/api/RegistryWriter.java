package dev.notalpha.dashloader.api;

public interface RegistryWriter {
	/**
	 * Adds an entry to the DashRegistry.
	 * @param object The object to add to the registry.
	 * @return An id to the registry entry, this is used to get back the original object on the cache read.
	 */
	<R> int add(final R object);

}
