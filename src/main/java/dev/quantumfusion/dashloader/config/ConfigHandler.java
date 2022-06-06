package dev.quantumfusion.dashloader.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

public class ConfigHandler {
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final Path configPath;
	public DashConfig config = new DashConfig();

	@Nullable
	private FileAlterationObserver observer;

	public ConfigHandler(Path configPath) {
		this.configPath = configPath;
	}


	public void reloadConfig() {
		try {
			if (Files.exists(this.configPath)) {
				final BufferedReader json = Files.newBufferedReader(this.configPath);
				this.config = this.gson.fromJson(json, DashConfig.class);
				json.close();
				return;
			}
		} catch (Throwable ignored) {
		}

		// if something fails or the file does not exist
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
