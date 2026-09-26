package dev.notalpha.dashloader.api;

import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.cache.CacheStatusBus;
import dev.notalpha.dashloader.client.DashLoaderClient;

/**
 * Status change notifications for other mods.
 *
 * <p>To read the current state once, use the cache directly:
 * {@code DashLoaderClient.CACHE.getStatus()} and {@code .getDir()}. This class
 * only exists for the part that is not reachable from there, reacting to status
 * changes without polling.
 *
 * <p>Example: do something once the cache is being served.
 * <pre>{@code
 * DashLoaderAPI.whenLoaded(() -> {
 *     // assets are now read from the cache
 * });
 * }</pre>
 *
 * <p>Example: follow the state.
 * <pre>{@code
 * DashLoaderAPI.addStatusListener(status -> LOG.info("DashLoader: {}", status));
 * }</pre>
 */
public final class DashLoaderAPI {
	private DashLoaderAPI() {
	}

	/**
	 * Runs the given action as soon as a cache is being served. If DashLoader is
	 * already loaded the action runs immediately on the calling thread, otherwise
	 * on the next status change to {@link CacheStatus#LOAD}.
	 *
	 * <p>Has no effect if DashLoader is set up to create a new cache, since that
	 * never reaches the loaded state.
	 *
	 * @param action The action to run.
	 */
	public static void whenLoaded(Runnable action) {
		if (DashLoaderClient.CACHE.getStatus() == CacheStatus.LOAD) {
			action.run();
			return;
		}
		CacheStatusBus.addListener(status -> {
			if (status == CacheStatus.LOAD) {
				action.run();
			}
		});
	}

	/**
	 * Runs the given action on every future status change. The current status is
	 * not reported. Listeners stay registered for the lifetime of the game and
	 * may be called from the caching thread.
	 *
	 * @param listener The listener which receives the new status.
	 */
	public static void addStatusListener(java.util.function.Consumer<CacheStatus> listener) {
		CacheStatusBus.addListener(listener);
	}
}
