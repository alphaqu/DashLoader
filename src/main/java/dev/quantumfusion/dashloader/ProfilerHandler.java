package dev.quantumfusion.dashloader;

import dev.quantumfusion.dashloader.util.TimeUtil;

import java.lang.management.ManagementFactory;

public class ProfilerHandler {
	public long reload_start = 0;
	public long export_time = -1;

	// File export
	public long export_file_reading_time = -1;
	public long export_asset_exporting_time = -1;
	public long export_asset_loading_time = -1;

	// Cache coverage
	public long fallback_models_count = -1;
	public long cached_models_count = -1;

	public long bootstrap_start = -1;
	public long bootstrap_end = -1;
	public long total_loading = -1;

	public void print() {
		DashLoader.LOG.info("┏ DashLoader Statistics.");
		if (this.export_time != -1) {
			DashLoader.LOG.info("┠──┬ {} DashLoader load", TimeUtil.getTimeString(this.export_time));
			DashLoader.LOG.info("┃  ├── {} File reading", TimeUtil.getTimeString(this.export_file_reading_time));
			DashLoader.LOG.info("┃  ├── {} Asset exporting", TimeUtil.getTimeString(this.export_asset_exporting_time));
			DashLoader.LOG.info("┃  └── {} Asset loading", TimeUtil.getTimeString(this.export_asset_loading_time));
			this.export_time = -1;
		}
		if (this.fallback_models_count != -1) {
			long totalModels = this.cached_models_count + this.fallback_models_count;
			DashLoader.LOG.info("┠──┬ {}% Cache coverage", (int) (((this.cached_models_count / (float) totalModels) * 100)));
			DashLoader.LOG.info("┃  ├── {} Fallback models", this.fallback_models_count);
			DashLoader.LOG.info("┃  └── {} Cached models", this.cached_models_count);
			this.cached_models_count = -1;
			this.fallback_models_count = -1;
		}
		DashLoader.LOG.info("┠── {} Minecraft client reload", TimeUtil.getTimeStringFromStart(this.reload_start));
		DashLoader.LOG.info("┠── {} Minecraft bootstrap", TimeUtil.getTimeString(this.bootstrap_end - this.bootstrap_start));

		if (this.total_loading == -1) {
			this.total_loading = ManagementFactory.getRuntimeMXBean().getUptime();
		}

		DashLoader.LOG.info("┖── {} Total loading", TimeUtil.getTimeString(this.total_loading));
	}
}
