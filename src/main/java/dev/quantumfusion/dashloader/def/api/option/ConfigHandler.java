package dev.quantumfusion.dashloader.def.api.option;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.quantumfusion.dashloader.def.DashConstants;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.api.option.data.DashConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;

public class ConfigHandler {
	public static DashConfig CONFIG = new DashConfig();
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

		updateFile();
	}

	public static void updateFile() {
		// update from config. Do it after mods because this has priority
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		final Path config = DashLoader.DASH_CONFIG_FOLDER.resolve("dashloader.json");
		if (Files.exists(config)) {
			try {
				CONFIG = gson.fromJson(Files.readString(config), DashConfig.class);
				if (CONFIG.disabledOptions != null) {
					for (Option disabledOption : CONFIG.disabledOptions)
						OPTION_ACTIVE.put(disabledOption, false);
				}

				if (CONFIG.disableWatermark)
					OPTION_ACTIVE.put(Option.WATERMARK, false);

				return;
			} catch (Throwable e) {
				DashLoader.LOGGER.error("Failed to read config file", e);
			}
		}

		try {
			Files.writeString(config, gson.toJson(CONFIG), StandardOpenOption.CREATE);
		} catch (IOException e) {
			DashLoader.LOGGER.error("Failed to create config file", e);
		}
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
