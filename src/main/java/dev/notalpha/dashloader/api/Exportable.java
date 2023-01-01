package dev.notalpha.dashloader.api;

import dev.notalpha.dashloader.registry.RegistryReader;

/**
 * The Exportable interface is the interface to implement when adding DashLoader cache support to a registry object.
 *
 * @param <R> Raw Object.
 */
@SuppressWarnings("unused")
public interface Exportable<R> {
	/**
	 * Runs before {@link Exportable#export(RegistryReader)} on the main thread.
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
	 * Runs after {@link Exportable#export(RegistryReader)} on the main thread.
	 */
	@SuppressWarnings("unused")
	default void postExport(RegistryReader reader) {
	}
}
