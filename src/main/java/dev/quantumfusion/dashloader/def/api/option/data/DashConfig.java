package dev.quantumfusion.dashloader.def.api.option.data;

import dev.quantumfusion.dashloader.def.api.option.Option;

import java.util.Map;

public class DashConfig {
	public Option[] disabledOptions = new Option[0];
	public boolean disableWatermark = false;
	public int cacheScreenLines = 100;
	public boolean debug = false;
	public int cacheScreenPaddingSize = 10;

	public String backgroundColor = "base1";
	public String textColor = "text";
	public String progressLaneColor = "base0";
	public String[] progressColors = new String[]{"red", "orange", "yellow", "green"};

	public Map<String, String> colors = Map.of(
			"red", "#ff6188",
			"orange", "#fc9867",
			"yellow", "#ffd866",
			"green", "#a9dc76",
			"blue", "#78dce8",
			"purple", "#ab9df2",
			"text", "#fcfcfa",
			"base0", "#19181a",
			"base1", "#221f22",
			"base2", "#2d2a2e");

	public Map<String, Integer> lineColors = Map.of(
			"base2", 1000,
			"blue", 50,
			"red", 1);


	public DashConfig() {
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public Option[] getDisabledOptions() {
		return disabledOptions;
	}

	public void setDisabledOptions(Option[] disabledOptions) {
		this.disabledOptions = disabledOptions;
	}

	public boolean isDisableWatermark() {
		return disableWatermark;
	}

	public void setDisableWatermark(boolean disableWatermark) {
		this.disableWatermark = disableWatermark;
	}

	public int getCacheScreenLines() {
		return cacheScreenLines;
	}

	public void setCacheScreenLines(int cacheScreenLines) {
		this.cacheScreenLines = cacheScreenLines;
	}

	public int getCacheScreenPaddingSize() {
		return cacheScreenPaddingSize;
	}

	public void setCacheScreenPaddingSize(int cacheScreenPaddingSize) {
		this.cacheScreenPaddingSize = cacheScreenPaddingSize;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	public String getProgressLaneColor() {
		return progressLaneColor;
	}

	public void setProgressLaneColor(String progressLaneColor) {
		this.progressLaneColor = progressLaneColor;
	}

	public String[] getProgressColors() {
		return progressColors;
	}

	public void setProgressColors(String[] progressColors) {
		this.progressColors = progressColors;
	}

	public Map<String, Integer> getLineColors() {
		return lineColors;
	}

	public void setLineColors(Map<String, Integer> lineColors) {
		this.lineColors = lineColors;
	}

	public Map<String, String> getColors() {
		return colors;
	}

	public void setColors(Map<String, String> colors) {
		this.colors = colors;
	}
}
