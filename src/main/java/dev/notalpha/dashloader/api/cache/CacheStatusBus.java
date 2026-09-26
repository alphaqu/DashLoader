package dev.notalpha.dashloader.api.cache;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Notifies listeners when the cache changes its status.
 *
 * <p>List is only used to fan out status changes; the cache itself is reachable
 * through {@code DashLoaderClient.CACHE}.
 */
public final class CacheStatusBus {
	private static final List<Consumer<CacheStatus>> LISTENERS = new CopyOnWriteArrayList<>();

	private CacheStatusBus() {
	}

	/**
	 * Registers a listener for status changes. Listeners may be called from the
	 * caching thread, so they must not assume they run on the main thread.
	 */
	public static void addListener(Consumer<CacheStatus> listener) {
		LISTENERS.add(listener);
	}

	/**
	 * Notifies all listeners. Called by the cache whenever its status changes.
	 */
	public static void fireStatus(CacheStatus status) {
		for (Consumer<CacheStatus> listener : LISTENERS) {
			listener.accept(status);
		}
	}
}
