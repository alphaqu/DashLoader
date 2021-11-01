package dev.quantumfusion.dashloader.def.corehook.holder;

import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.hyphen.scan.annotations.Data;

import java.util.List;

@Data
public class DashSplashTextData {
	public final List<String> splashList;

	public DashSplashTextData(List<String> splashList) {
		this.splashList = splashList;
	}

	public DashSplashTextData(DashDataManager data) {
		splashList = data.splashText.getMinecraftData();
	}

	public List<String> export() {
		return splashList;
	}


}
