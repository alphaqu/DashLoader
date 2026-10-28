package dev.notalpha.dashloader.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.notalpha.dashloader.DashLoader;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ConfigHandler {
	private static final EnumMap<Option, Boolean> OPTION_ACTIVE = new EnumMap<>(Option.class);
	private static final String DISABLE_OPTION_TAG = "dashloader:disableoption";

	static {
		for (Option value : Option.values()) {
			OPTION_ACTIVE.put(value, value != Option.CACHE_MODEL_LOADER);
		}
	}

	public static final ConfigHandler INSTANCE = new ConfigHandler(FabricLoader.getInstance().getConfigDir().normalize().resolve("dashloader.json"));

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final Path configPath;
	public volatile Config config = new Config();

	public ConfigHandler(Path configPath) {
		this.configPath = configPath;
		this.reloadConfig();
		List<String> removedOptions = new ArrayList<>();
		this.config.options.forEach((s, aBoolean) -> {
			try {
				var option = Option.valueOf(s.toUpperCase());
				OPTION_ACTIVE.put(option, aBoolean);
				if (!aBoolean) {
					DashLoader.LOG.warn("Disabled Optional Feature {} from DashLoader config.", s);
				}
			} catch (IllegalArgumentException illegalArgumentException) {
			removedOptions.add(s);
			}
		});
		removedOptions.forEach(this.config.options::remove);

		for (var modContainer : FabricLoader.getInstance().getAllMods()) {
			var mod = modContainer.getMetadata();
			if (mod.containsCustomValue(DISABLE_OPTION_TAG)) {
				for (var value : mod.getCustomValue(DISABLE_OPTION_TAG).getAsArray()) {
					final String feature = value.getAsString();
					try {
						var option = Option.valueOf(feature.toUpperCase());
						OPTION_ACTIVE.put(option, false);
						DashLoader.LOG.warn("Disabled Optional Feature {} from {} config. {}", feature, mod.getId(), mod.getName());
					} catch (IllegalArgumentException illegalArgumentException) {
						DashLoader.LOG.warn("Mod {} asked to disable Optional Feature {} which does not exist.", mod.getId(), feature);
					}
				}
			}
		}
		if (isVulkanModPresent()) {
			OPTION_ACTIVE.put(Option.UNSAFE_MIPMAP_GENERATION, false);
			DashLoader.LOG.warn("Found VulkanMod, Disabling Optional Feature {}", Option.UNSAFE_MIPMAP_GENERATION.name());
		}
		if (isQuiltLoaderPresent()) {
			// Quilt ships its own MixinExtras/ASM combo that breaks @Redirect processing on
			// MipmapGenerator (ClassCastException in MixinExtras transformer -> crash while
			// generating mipmaps). Skip the unsafe mipmap mixin; vanilla mipmaps are used instead.
			OPTION_ACTIVE.put(Option.UNSAFE_MIPMAP_GENERATION, false);
			DashLoader.LOG.warn("Found Quilt Loader, Disabling Optional Feature {}", Option.UNSAFE_MIPMAP_GENERATION.name());
		}
	}

	public static boolean shouldApplyMixin(String name) {
		for (Option value : Option.values()) {
			if (name.contains(value.mixinContains)) {
				return OPTION_ACTIVE.get(value);
			}
		}
		return true;
	}

	public static boolean optionActive(Option option) {
		return OPTION_ACTIVE.get(option);
	}

	public void reloadConfig() {
		if (Files.exists(this.configPath)) {
			// gson returns null for an empty file and for a literal "null", and a
			// partially invalid document can deserialize into an instance with null
			// fields. Both used to end up as a NullPointerException later on.
			try (BufferedReader json = Files.newBufferedReader(this.configPath)) {
				Config read = this.gson.fromJson(json, Config.class);
				if (read != null) {
					this.config = read;
				} else {
					DashLoader.LOG.warn("Config was empty, creating a new one.");
				}
			} catch (Throwable err) {
				DashLoader.LOG.warn("Config corrupted, creating a new one.", err);
				this.config = new Config();
			}
		}

		if (this.config.options == null) {
			this.config.options = new LinkedHashMap<>();
		}
		if (this.config.customSplashLines == null) {
			this.config.customSplashLines = new ArrayList<>();
		}
		if (this.config.compression < 0 || this.config.compression > 22) {
			DashLoader.LOG.warn("Invalid compression level {}, falling back to 1", this.config.compression);
			this.config.compression = 1;
		}

		this.saveConfig();
	}

	public void saveConfig() {
		// Written to a temp file and moved into place. Truncating the real config
		// first leaves an empty file behind if the game dies in between, which then
		// reads back as a null config.
		Path tmpPath = this.configPath.resolveSibling(this.configPath.getFileName() + ".tmp");
		try {
			Files.createDirectories(this.configPath.getParent());
			try (BufferedWriter writer = Files.newBufferedWriter(tmpPath, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
				this.gson.toJson(this.config, writer);
			}
			Files.move(tmpPath, this.configPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			DashLoader.LOG.error("Could not save config", e);
			try {
				Files.deleteIfExists(tmpPath);
			} catch (IOException ignored) {
			}
		}
	}

	private static boolean isVulkanModPresent() {
		return FabricLoader.getInstance().isModLoaded("vulkanmod");
	}

	private static boolean isQuiltLoaderPresent() {
		return FabricLoader.getInstance().isModLoaded("quilt_loader");
	}
}
