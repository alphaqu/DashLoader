package dev.quantumfusion.dashloader;

import dev.quantumfusion.dashloader.util.TimeUtil;

import java.lang.management.ManagementFactory;
import static dev.quantumfusion.dashloader.DashLoader.DL;

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
		DL.log.info("┏ DashLoader Statistics.");
		if (this.export_time != -1) {
			DL.log.info("┠──┬ {} DashLoader load", TimeUtil.getTimeString(this.export_time));
			DL.log.info("┃  ├── {} File reading", TimeUtil.getTimeString(this.export_file_reading_time));
			DL.log.info("┃  ├── {} Asset exporting", TimeUtil.getTimeString(this.export_asset_exporting_time));
			DL.log.info("┃  └── {} Asset loading", TimeUtil.getTimeString(this.export_asset_loading_time));
			this.export_time = -1;
		}
		if (this.fallback_models_count != -1) {
			long totalModels = this.cached_models_count + this.fallback_models_count;
			DL.log.info("┠──┬ {}% Cache coverage", (int) (((this.cached_models_count / (float) totalModels) * 100)));
			DL.log.info("┃  ├── {} Fallback models", this.fallback_models_count);
			DL.log.info("┃  └── {} Cached models", this.cached_models_count);
			this.cached_models_count = -1;
			this.fallback_models_count = -1;
		}
		DL.log.info("┠── {} Minecraft client reload", TimeUtil.getTimeStringFromStart(this.reload_start));
		DL.log.info("┠── {} Minecraft bootstrap", TimeUtil.getTimeString(this.bootstrap_end - this.bootstrap_start));

		if (this.total_loading == -1) {
			this.total_loading = ManagementFactory.getRuntimeMXBean().getUptime();
		}

		DL.log.info("┖── {} Total loading", TimeUtil.getTimeString(this.total_loading));
	}
}
