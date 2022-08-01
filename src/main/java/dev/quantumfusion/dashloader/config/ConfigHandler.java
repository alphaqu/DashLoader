package dev.quantumfusion.dashloader.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.quantumfusion.dashloader.DashConstants;
import dev.quantumfusion.dashloader.api.option.Option;
import java.util.EnumMap;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;
import static dev.quantumfusion.dashloader.DashLoader.DL;

public class ConfigHandler {
	private static final EnumMap<Option, Boolean> OPTION_ACTIVE = new EnumMap<>(Option.class);
	private static final String DISABLE_OPTION_TAG = DashConstants.DASH_DISABLE_OPTION_TAG;
	private final Gson gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
	private final Path configPath;
	public DashConfig config = new DashConfig();

	@Nullable
	private FileAlterationObserver observer;

	public ConfigHandler(Path configPath) {
		this.configPath = configPath;
		this.reloadConfig();
		this.config.options.forEach((s, aBoolean) -> {
			try {
				var option = Option.valueOf(s.toUpperCase());
				OPTION_ACTIVE.put(option, false);
				DL.log.warn("Disabled Optional Feature {} from DashLoader config.", s);
			} catch (IllegalArgumentException illegalArgumentException) {
				DL.log.error("Could not disable Optional Feature {} as it does not exist.", s);
			}
		});

		for (var modContainer : FabricLoader.getInstance().getAllMods()) {
			var mod = modContainer.getMetadata();
			if (mod.containsCustomValue(DISABLE_OPTION_TAG)) {
				for (var value : mod.getCustomValue(DISABLE_OPTION_TAG).getAsArray()) {
					final String feature = value.getAsString();
					try {
						var option = Option.valueOf(feature.toUpperCase());
						OPTION_ACTIVE.put(option, false);
						DL.log.warn("Disabled Optional Feature {} from {} config. {}", feature, mod.getId(), mod.getName());
					} catch (IllegalArgumentException illegalArgumentException) {
						DL.log.error("Could not disable Optional Feature {} as it does not exist.", feature);
					}
				}
			}
		}
	}


	public void reloadConfig() {
		try {
			if (Files.exists(this.configPath)) {
				final BufferedReader json = Files.newBufferedReader(this.configPath);
				this.config = this.gson.fromJson(json, DashConfig.class);
				json.close();
			}
		} catch (Throwable err) {
			DL.log.info("Config corrupted creating a new one.", err);
		}

		this.saveConfig();
	}

	public void saveConfig() {
		try {
			Files.createDirectories(this.configPath.getParent());
			final BufferedWriter writer = Files.newBufferedWriter(this.configPath, StandardOpenOption.CREATE);
			this.gson.toJson(this.config, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
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


	static {
		for (Option value : Option.values()) {
			OPTION_ACTIVE.put(value, true);
		}
	}

	public void addListener(Consumer<DashConfig> configListener) {
		if (this.observer == null) {
			File directory = this.configPath.getParent().toFile();
			this.observer = new FileAlterationObserver(directory);
			FileAlterationMonitor monitor = new FileAlterationMonitor(100);
			monitor.addObserver(this.observer);
			try {
				monitor.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		this.observer.addListener(new FileAlterationListener() {
			@Override
			public void onStart(FileAlterationObserver observer) {

			}

			@Override
			public void onDirectoryCreate(File directory) {

			}

			@Override
			public void onDirectoryChange(File directory) {

			}

			@Override
			public void onDirectoryDelete(File directory) {

			}

			@Override
			public void onFileCreate(File file) {

			}

			@Override
			public void onFileChange(File file) {
				try {
					if (Files.isSameFile(Path.of(file.toURI()), ConfigHandler.this.configPath)) {
						ConfigHandler.this.reloadConfig();
						configListener.accept(ConfigHandler.this.config);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFileDelete(File file) {

			}

			@Override
			public void onStop(FileAlterationObserver observer) {

			}
		});
	}
}
