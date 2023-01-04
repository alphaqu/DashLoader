package dev.notalpha.dashloader.misc;

import dev.notalpha.dashloader.Cache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class OptionData<D> {
	@Nullable
	private D data;

	private Cache cacheManager;

	@Nullable
	private Cache.Status dataStatus;

	@Nullable
	private final Cache.Status onlyOn;

	public OptionData(@Nullable Cache.Status onlyOn) {
		this.data = null;
		this.onlyOn = onlyOn;
	}

	public OptionData() {
		this(null);
	}

	public void visit(Cache.Status status, Consumer<D> consumer) {
		if (this.active(status)) {
			consumer.accept(this.data);
		}
	}


	/**
	 * Gets the value or returns null if its status does not match the current state.
	 **/
	public @Nullable D get(Cache.Status status) {
		if (this.active(status)) {
			return this.data;
		}
		return null;
	}

	public void reset(Cache cacheManager, @NotNull D data) {
		this.cacheManager = cacheManager;
		set(cacheManager.getStatus(), data);
	}

	/**
	 * Sets the optional data to the intended status
	 **/
	public void set(Cache.Status status, @NotNull D data) {
		if (onlyOn != null && onlyOn != status) {
			this.data = null;
			this.dataStatus = null;
			return;
		}

		if (cacheManager == null) {
			throw new RuntimeException("cacheManager is null. This OptionData has never been reset in its handler.");
		}

		Cache.Status currentStatus = cacheManager.getStatus();
		if (status == currentStatus) {
			this.dataStatus = status;
			this.data = data;
		}
	}

	public boolean active(Cache.Status status) {
		return status == this.dataStatus && status == cacheManager.getStatus() && this.data != null && (onlyOn == null || onlyOn == status);
	}
}
