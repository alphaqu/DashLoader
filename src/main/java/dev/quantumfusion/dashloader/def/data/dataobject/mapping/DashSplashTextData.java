package dev.quantumfusion.dashloader.def.data.dataobject.mapping;

import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.VanillaData;
import dev.quantumfusion.hyphen.scan.annotations.Data;

import java.util.List;

@Data
public class DashSplashTextData {
	public final List<String> splashList;

	public DashSplashTextData(List<String> splashList) {
		this.splashList = splashList;
	}

	public DashSplashTextData(VanillaData data) {
		splashList = data.getSplashText();
	}

	public List<String> export() {
		return splashList;
	}


}
