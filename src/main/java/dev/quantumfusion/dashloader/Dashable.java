package dev.quantumfusion.dashloader;

import dev.quantumfusion.dashloader.registry.RegistryReader;

/**
 * The Dashable interface is the interface to implement when adding DashLoader cache support to a registry object.
 *
 * @param <R> Raw Object.
 */
public interface Dashable<R> {
	/**
	 * Runs before {@link Dashable#export(RegistryReader)} on the main thread.
	 */
	default void preExport(RegistryReader reader) {
	}

	/**
	 * Runs in parallel returning the target object.
	 */
	R export(RegistryReader reader);

	/**
	 * Runs after {@link Dashable#export(RegistryReader)} on the main thread.
	 */
	default void postExport(RegistryReader reader) {
	}
}
