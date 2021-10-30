package dev.quantumfusion.dashloader.def.api.feature;

import java.util.HashMap;
import java.util.Map;

/**
 * smol class
 */
public class FeatureHandler {
	public static long data = 0;
	public static Map<String, Feature> mixinCache;
	public static Map<String, Feature> nameCache;


	public static void init() {
		mixinCache = new HashMap<>();
		nameCache = new HashMap<>();
		for (Feature value : Feature.values()) {
			FeatureHandler.mixinCache.put("dev.quantumfusion.dashloader.def.mixin.feature." + value.mixin, value);
			FeatureHandler.nameCache.put(value.name(), value);
		}
	}

	public static int calculateTasks() {
		//value is the static tasks that dashloader has
		int out = 5;
		for (Feature value : Feature.values()) {
			if (value.active()) {
				out += value.tasks;
			}
		}
		return out;
	}

	public static void disableFeature(Feature feature) {
		if (feature != null) {
			data |= 1L << feature.ordinal();
		}
	}

	public static void disableFeature(String feature) {
		disableFeature(nameCache.get(feature));
	}

	public static boolean isFeatureActive(Feature feature) {
		return ((data >> feature.ordinal()) & 1L) == 0;
	}

	public static boolean active(String mixin) {
		Feature feature = mixinCache.get(mixin);
		if (feature == null) {
			for (Map.Entry<String, Feature> stringFeatureEntry : mixinCache.entrySet()) {
				if (mixin.startsWith(stringFeatureEntry.getKey())) {
					feature = stringFeatureEntry.getValue();
				}
			}
		}
		return feature == null || feature.active();
	}

}
