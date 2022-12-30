package dev.quantumfusion.dashloader.api;

import dev.quantumfusion.dashloader.registry.RegistryReader;

/**
 * The Dashable interface is the interface to implement when adding DashLoader cache support to a registry object.
 *
 * @param <R> Raw Object.
 */
@SuppressWarnings("unused")
public interface Dashable<R> {
	/**
	 * Runs before {@link Dashable#export(RegistryReader)} on the main thread.
	 */
	@SuppressWarnings("unused")
	default void preExport(RegistryReader reader) {
	}

	/**
	 * Runs in parallel returning the target object.
	 */
	@SuppressWarnings("unused")
	R export(RegistryReader reader);

	/**
	 * Runs after {@link Dashable#export(RegistryReader)} on the main thread.
	 */
	@SuppressWarnings("unused")
	default void postExport(RegistryReader reader) {
	}
}
