package dev.quantumfusion.dashloader.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashConfig {
	public Map<String, Boolean> options = new LinkedHashMap<>();
	public byte compression = 3;

	// ==================================== Screen ====================================
	public boolean debugMode = false;
	public int paddingSize = 10;

	// Colors
	public String backgroundColor = "base1";
	public String foregroundColor = "text";
	public Map<String, String> colorVariables = Map.of(
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

	// Progress bar
	public int progressBarHeight = 2;
	public int progressBarSpeedDivision = 10;
	public String[] progressColors = new String[]{"red", "orange", "yellow", "green"};
	public String progressTrackColor = "base0";

	// Lines
	public int lineAmount = 200;
	public int lineWidth = 100;
	public int lineMinHeight = 4;
	public int lineMaxHeight = 10;
	public float lineSpeed = 2;
	public float lineSpeedDifference = 4;
	public String lineDirection = "LEFT";
	public Map<String, Integer> lineColors = Map.of(
			"base2", 1000,
			"blue", 50,
			"red", 1);

	public List<String> customSplashLines = new ArrayList<>();
	public boolean addDefaultSplashLines = true;


}
