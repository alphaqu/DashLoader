package dev.notalpha.dashloader.api.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("CanBeFinal")
public class Config {
	public Map<String, Boolean> options = new LinkedHashMap<>();
	public byte compression = 3;
	public List<String> customSplashLines = new ArrayList<>();
	public boolean addDefaultSplashLines = true;
}
