package dev.quantumfusion.dashloader;

import dev.quantumfusion.taski.ParentTask;
import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.AbstractTask;
import dev.quantumfusion.taski.builtin.StaticTask;

import java.util.HashMap;

public final class ProgressHandler {
	public static ProgressHandler INSTANCE = new ProgressHandler();
	public Task task = new StaticTask("Idle", 0);
	private String overwriteText;

	private long lastUpdate = System.currentTimeMillis();
	private double currentProgress = 0;

	private HashMap<String, String> translations = new HashMap<>();

	private ProgressHandler() {
	}

	public void setTranslations(HashMap<String, String> translations) {
		this.translations = translations;
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
		return this.translations.getOrDefault(text, text);
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
			if (depth > 1)  {
				String subName = concatTask(depth - 1, subTask);
				if (subName != null)  {
					return name + "." + subName;
				}
			}
		}

		return name;
	}

	private String getProgressText(int depth, Task task) {
		if (task instanceof ParentTask stepTask) {
			Task subTask = stepTask.getChild();
			if (depth > 1)  {
				String subName = getProgressText(depth - 1, subTask);
				if (subName != null)  {
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
		this.overwriteText = this.translations.getOrDefault(overwriteText, overwriteText);
	}
}
