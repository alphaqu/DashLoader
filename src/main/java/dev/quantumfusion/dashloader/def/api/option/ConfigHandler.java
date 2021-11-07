package dev.quantumfusion.dashloader.def.api.option;

import dev.quantumfusion.dashloader.core.client.config.DashConfigHandler;
import dev.quantumfusion.dashloader.def.DashConstants;
import dev.quantumfusion.dashloader.def.DashLoader;
import net.fabricmc.loader.api.FabricLoader;

import java.util.EnumMap;

public class ConfigHandler {
	public static boolean pathSet = false;
	private static final EnumMap<Option, Boolean> OPTION_ACTIVE = new EnumMap<>(Option.class);
	private static final String OPTION_TAG = DashConstants.DASH_OPTION_TAG;

	public static void update() {
		// update all fabric mods and such, config has priority
		for (var modContainer : FabricLoader.getInstance().getAllMods()) {
			var mod = modContainer.getMetadata();
			if (mod.containsCustomValue(OPTION_TAG)) {
				for (var value : mod.getCustomValue(OPTION_TAG).getAsArray()) {
					final String feature = value.getAsString();
					try {
						var option = Option.valueOf(feature.toUpperCase());
						OPTION_ACTIVE.put(option, false);
						DashLoader.LOGGER.warn("Disable Optional Feature {} from {} config. {}", feature, mod.getId(), mod.getName());
					} catch (IllegalArgumentException illegalArgumentException) {
						DashLoader.LOGGER.error("Could not disable Optional Feature {} as it does not exist.", feature);
					}
				}
			}
		}

		if (!pathSet) {
			DashConfigHandler.INSTANCE.setConfigPath(DashLoader.DASH_CONFIG_FOLDER.resolve("dashloader.json"));
		}

		System.out.println("Reloading config");
		DashConfigHandler.INSTANCE.reloadConfig();
	}

	public static boolean shouldApplyMixin(String name) {
		for (Option value : Option.values()) {
			if (name.contains(value.mixinContains)) return OPTION_ACTIVE.get(value);
		}
		return true;
	}

	public static boolean optionActive(Option option) {
		return OPTION_ACTIVE.get(option);
	}


	static {
		for (Option value : Option.values()) OPTION_ACTIVE.put(value, true);
	}
}
