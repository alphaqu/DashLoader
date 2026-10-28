package dev.notalpha.dashloader.api;

import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.cache.CacheStatusBus;
import dev.notalpha.dashloader.client.DashLoaderClient;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
	 * Runs the given action as soon as a cache is being served. Fires exactly
	 * once: if DashLoader is already loaded the action runs immediately on the
	 * calling thread, otherwise on the next status change to
	 * {@link CacheStatus#LOAD}.
	 *
	 * <p>The listener unsubscribes itself, so registering this again after a
	 * {@code F3+T} reload runs the action once more, but a single registration
	 * never fires twice.
	 *
	 * <p>Has no effect if DashLoader is set up to create a new cache, since that
	 * never reaches the loaded state.
	 *
	 * @param action The action to run.
	 */
	public static void whenLoaded(Runnable action) {
		AtomicBoolean fired = new AtomicBoolean();
		AtomicReference<AutoCloseable> handle = new AtomicReference<>();
		handle.set(CacheStatusBus.addListener(status -> {
			if (status == CacheStatus.LOAD && fired.compareAndSet(false, true)) {
				closeQuietly(handle.get());
				action.run();
			}
		}));

		// Subscribing first and only then looking at the status closes the window
		// in which the status changed to LOAD between the check and the subscribe.
		// Losing that race means the action would never run at all.
		if (DashLoaderClient.CACHE.getStatus() == CacheStatus.LOAD && fired.compareAndSet(false, true)) {
			closeQuietly(handle.get());
			action.run();
		}
	}

	private static void closeQuietly(AutoCloseable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (Exception ignored) {
		}
	}

	/**
	 * Runs the given action on every future status change. The current status is
	 * not reported. Listeners may be called from the caching thread.
	 *
	 * <p>Use {@link #whenLoaded(Runnable)} for a one-shot reaction, this listener
	 * stays registered until the returned handle is closed.
	 *
	 * @param listener The listener which receives the new status.
	 * @return a handle that removes the listener again when closed.
	 */
	public static AutoCloseable addStatusListener(java.util.function.Consumer<CacheStatus> listener) {
		return CacheStatusBus.addListener(listener);
	}
}
