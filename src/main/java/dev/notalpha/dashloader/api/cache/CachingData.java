package dev.notalpha.dashloader.api.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CachingData<D> {
	@Nullable
	private D data;

	private DashCache cache;

	@Nullable
	private CacheStatus dataStatus;

	@Nullable
	private final CacheStatus onlyOn;

	public CachingData(@Nullable CacheStatus onlyOn) {
		this.data = null;
		this.onlyOn = onlyOn;
	}

	public CachingData() {
		this(null);
	}

	public void visit(CacheStatus status, Consumer<D> consumer) {
		if (this.active(status)) {
			consumer.accept(this.data);
		}
	}


	/**
	 * Gets the value or returns null if its status does not match the current state.
	 **/
	public @Nullable D get(CacheStatus status) {
		if (this.active(status)) {
			return this.data;
		}
		return null;
	}

	public void reset(DashCache cache, @NotNull D data) {
		this.cache = cache;
		set(cache.getStatus(), data);
	}

	/**
	 * Sets the optional data to the intended status
	 **/
	public void set(CacheStatus status, @NotNull D data) {
		if (onlyOn != null && onlyOn != status) {
			this.data = null;
			this.dataStatus = null;
			return;
		}

		if (cache == null) {
			throw new RuntimeException("cacheManager is null. This OptionData has never been reset in its handler.");
		}

		CacheStatus currentStatus = cache.getStatus();
		if (status == currentStatus) {
			this.dataStatus = status;
			this.data = data;
		}
	}

	public boolean active(CacheStatus status) {
		return status == this.dataStatus && status == cache.getStatus() && this.data != null && (onlyOn == null || onlyOn == status);
	}
}
