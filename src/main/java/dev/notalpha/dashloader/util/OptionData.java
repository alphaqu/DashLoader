package dev.notalpha.dashloader.util;

import dev.notalpha.dashloader.cache.CacheManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class OptionData<D> {
	@Nullable
	private D data;

	private CacheManager cacheManager;

	@Nullable
	private CacheManager.Status dataStatus;

	@Nullable
	private final CacheManager.Status onlyOn;

	public OptionData(@Nullable CacheManager.Status onlyOn) {
		this.data = null;
		this.onlyOn = onlyOn;
	}

	public OptionData() {
		this(null);
	}

	public void visit(CacheManager.Status status, Consumer<D> consumer) {
		if (this.active(status)) {
			consumer.accept(this.data);
		}
	}


	/**
	 * Gets the value or returns null if its status does not match the current state.
	 **/
	public @Nullable D get(CacheManager.Status status) {
		if (this.active(status)) {
			return this.data;
		}
		return null;
	}

	public void reset(CacheManager cacheManager, @NotNull D data) {
		this.cacheManager = cacheManager;
		set(cacheManager.getStatus(), data);
	}

	/**
	 * Sets the optional data to the intended status
	 **/
	public void set(CacheManager.Status status, @NotNull D data) {
		if (onlyOn != null && onlyOn != status) {
			this.data = null;
			this.dataStatus = null;
			return;
		}

		CacheManager.Status currentStatus = cacheManager.getStatus();
		if (status == currentStatus) {
			this.dataStatus = status;
			this.data = data;
		}
	}

	public boolean active(CacheManager.Status status) {
		return status == this.dataStatus && status == cacheManager.getStatus() && this.data != null && (onlyOn == null || onlyOn == status);
	}
}
