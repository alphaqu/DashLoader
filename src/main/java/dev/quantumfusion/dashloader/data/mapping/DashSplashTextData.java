package dev.quantumfusion.dashloader.data.mapping;

import dev.quantumfusion.dashloader.DashDataManager;

import java.util.List;

public class DashSplashTextData {
	public final List<String> splashList;

	public DashSplashTextData(List<String> splashList) {
		this.splashList = splashList;
	}

	public DashSplashTextData(DashDataManager data) {
		this.splashList = data.splashText.getMinecraftData();
	}

	public List<String> export() {
		return this.splashList;
	}


}
