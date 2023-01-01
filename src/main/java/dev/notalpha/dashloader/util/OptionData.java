package dev.notalpha.dashloader.util;

import dev.notalpha.dashloader.DashLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class OptionData<D> {
	@Nullable
	private D data;
	@Nullable
	private DashLoader.Status dataStatus;

	@Nullable
	private final DashLoader.Status onlyOn;

	public OptionData( @Nullable DashLoader.Status onlyOn) {
		this.data = null;
		this.onlyOn = onlyOn;
	}
	public OptionData() {
		this(null);
	}


	public void visit(DashLoader.Status status, Consumer<D> consumer) {
		if (this.active(status)) {
			consumer.accept(this.data);
		}
	}


	/** Gets the value or returns null if its status does not match the current state. **/
	public @Nullable D get(DashLoader.Status status) {
		if (this.active(status)) {
			return this.data;
		}
		return null;
	}

	/** Sets the optional data to the intended status **/
	public void set(DashLoader.Status status, @NotNull D data) {
		if (onlyOn != null && onlyOn != status) {
			this.data = null;
			this.dataStatus = null;
			return;
		}

		DashLoader.Status currentStatus = DashLoader.INSTANCE.getStatus();
		if (status == currentStatus) {
			this.dataStatus = status;
			this.data = data;
		}
	}

	public boolean active(DashLoader.Status status) {
		DashLoader.Status currentStatus = DashLoader.INSTANCE.getStatus();
		return status == this.dataStatus && status == currentStatus && this.data != null && (onlyOn == null || onlyOn == status);
	}
}
