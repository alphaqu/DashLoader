package dev.notalpha.dashloader.api.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOG = LoggerFactory.getLogger("dashloader-api");
	private static final List<Consumer<CacheStatus>> LISTENERS = new CopyOnWriteArrayList<>();

	private CacheStatusBus() {
	}

	/**
	 * Registers a listener for status changes. Listeners may be called from the
	 * caching thread, so they must not assume they run on the main thread.
	 *
	 * @return a handle that removes the listener again when closed.
	 */
	public static AutoCloseable addListener(Consumer<CacheStatus> listener) {
		LISTENERS.add(listener);
		return () -> LISTENERS.remove(listener);
	}

	/**
	 * Notifies all listeners. Called by the cache whenever its status changes.
	 *
	 * <p>A failing listener is logged and skipped. It must not abort the loop,
	 * because this runs inside {@code Cache.setStatus}: an exception here would
	 * leave the remaining listeners unnotified and break the loading cache itself.
	 */
	public static void fireStatus(CacheStatus status) {
		for (Consumer<CacheStatus> listener : LISTENERS) {
			try {
				listener.accept(status);
			} catch (Throwable thr) {
				LOG.error("DashLoader status listener {} failed", listener, thr);
			}
		}
	}
}