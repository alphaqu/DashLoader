package dev.notalpha.dashloader;

import dev.notalpha.dashloader.util.TimeUtil;

import java.lang.management.ManagementFactory;

public class ProfilerHandler {
	public static final ProfilerHandler INSTANCE = new ProfilerHandler();
	public long reloadStart = 0;
	public long exportTime = -1;

	// File export
	public long exportFileReadingTime = -1;
	public long exportAssetExportingTime = -1;
	public long exportAssetLoadingTime = -1;

	// Cache coverage
	public long fallbackModelsCount = -1;
	public long cachedModelsCount = -1;

	public long bootstrapStart = -1;
	public long bootstrapEnd = -1;
	public long totalLoading = -1;

	private ProfilerHandler() {
	}

	public void print() {
		DashLoader.LOG.info("┏ DashLoader Statistics.");
		if (this.exportTime != -1) {
			DashLoader.LOG.info("┠──┬ {} DashLoader load", TimeUtil.getTimeString(this.exportTime));
			DashLoader.LOG.info("┃  ├── {} File reading", TimeUtil.getTimeString(this.exportFileReadingTime));
			DashLoader.LOG.info("┃  ├── {} Asset exporting", TimeUtil.getTimeString(this.exportAssetExportingTime));
			DashLoader.LOG.info("┃  └── {} Asset loading", TimeUtil.getTimeString(this.exportAssetLoadingTime));
			this.exportTime = -1;
		}
		if (this.fallbackModelsCount != -1) {
			long totalModels = this.cachedModelsCount + this.fallbackModelsCount;
			DashLoader.LOG.info("┠──┬ {}% Cache coverage", (int) (((this.cachedModelsCount / (float) totalModels) * 100)));
			DashLoader.LOG.info("┃  ├── {} Fallback models", this.fallbackModelsCount);
			DashLoader.LOG.info("┃  └── {} Cached models", this.cachedModelsCount);
			this.cachedModelsCount = -1;
			this.fallbackModelsCount = -1;
		}
		DashLoader.LOG.info("┠── {} Minecraft client reload", TimeUtil.getTimeStringFromStart(this.reloadStart));
		DashLoader.LOG.info("┠── {} Minecraft bootstrap", TimeUtil.getTimeString(this.bootstrapEnd - this.bootstrapStart));

		if (this.totalLoading == -1) {
			this.totalLoading = ManagementFactory.getRuntimeMXBean().getUptime();
		}

		DashLoader.LOG.info("┖── {} Total loading", TimeUtil.getTimeString(this.totalLoading));
	}
}
