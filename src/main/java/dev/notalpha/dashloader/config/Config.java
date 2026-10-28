package dev.notalpha.dashloader.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("CanBeFinal")
public class Config {
	public Map<String, Boolean> options = new LinkedHashMap<>();
	// Zstd level 1 is about 2.8x faster than level 3 for only ~7% worse
	// compression. Caching is a write-once-read-many case, so the speed wins.
	public byte compression = 1;
	public int maxCaches = 5;
	public List<String> customSplashLines = new ArrayList<>();
	public boolean addDefaultSplashLines = true;
	public boolean singleThreadedReading = false;
	public boolean showCachingToast = true;
}
