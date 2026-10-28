package dev.notalpha.dashloader.client.ui.toast;

import dev.notalpha.dashloader.misc.TranslationHelper;
import dev.notalpha.taski.ParentTask;
import dev.notalpha.taski.Task;
import dev.notalpha.taski.builtin.AbstractTask;
import dev.notalpha.taski.builtin.StaticTask;

public final class DashToastState {
	private final TranslationHelper translations;
	// Written by the cache thread, read by the render thread. currentProgress and
	// lastUpdate stay plain fields, they are only touched by the render thread.
	public volatile Task task = new StaticTask("Idle", 0);
	private volatile String overwriteText;
	private volatile DashToastStatus status;
	private double currentProgress = 0;
	private long lastUpdate = System.currentTimeMillis();
	private volatile long timeDone = System.currentTimeMillis();

	public DashToastState() {
		// Must stay on the client thread, TranslationHelper reads the language manager.
		this.translations = TranslationHelper.getInstance();
	}

	private void tickProgress() {
		if (Double.isNaN(this.currentProgress)) {
			this.currentProgress = 0.0;
		}
		final double actualProgress = task.getProgress();
		final double divisionSpeed = (actualProgress < this.currentProgress) ? 3 : 30;
		double currentProgress1 = (actualProgress - this.currentProgress) / divisionSpeed;
		this.currentProgress += currentProgress1;
	}

	public double getProgress() {
		final long currentTime = System.currentTimeMillis();
		while (currentTime > this.lastUpdate) {
			this.tickProgress();
			this.lastUpdate += 10; // ~100ups
		}
		return this.currentProgress;
	}

	public String getText() {
		if (this.overwriteText != null) {
			return this.overwriteText;
		}

		String text = concatTask(3, task);
		return this.translations.get(text);
	}

	public String getProgressText() {
		return this.getProgressText(3, task);
	}

	private String concatTask(int depth, Task task) {
		String name = null;
		if (task instanceof AbstractTask abstractTask) {
			name = abstractTask.getName();
		}

		if (task instanceof ParentTask stepTask) {
			Task subTask = stepTask.getChild();
			if (depth > 1) {
				String subName = concatTask(depth - 1, subTask);
				if (subName != null) {
					return name + "." + subName;
				}
			}
		}

		return name;
	}

	private String getProgressText(int depth, Task task) {
		if (task instanceof ParentTask stepTask) {
			Task subTask = stepTask.getChild();
			if (depth > 1) {
				String subName = getProgressText(depth - 1, subTask);
				if (subName != null) {
					return subName;
				}
			}
		}

		if (task instanceof AbstractTask abstractTask) {
			return abstractTask.getProgressText();
		}
		return null;
	}

	public void setOverwriteText(String overwriteText) {
		this.overwriteText = this.translations.get(overwriteText);
	}

	public DashToastStatus getStatus() {
		return status;
	}

	public void setStatus(DashToastStatus status) {
		this.status = status;
	}

	public long getTimeDone() {
		return timeDone;
	}

	/**
	 * Publishes a finished toast: stamps the done time and the new status in one
	 * step, with the status written last.
	 * <p>
	 * The cache thread writes {@link #overwriteText} and {@link #timeDone} while
	 * the render thread reads them, so the status must be the final write. It is
	 * the flag the render thread reacts to, and the volatile write publishes
	 * everything in front of it. Writing the status first instead lets the render
	 * thread observe {@link DashToastStatus#DONE} next to a stale done time, which
	 * hides the toast again immediately.
	 */
	public void setFinished(DashToastStatus status) {
		this.timeDone = System.currentTimeMillis();
		this.status = status;
	}
}
