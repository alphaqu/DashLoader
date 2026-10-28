package dev.notalpha.dashloader.client.ui;

import dev.notalpha.dashloader.client.ui.widget.ConfigListWidget;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.config.Option;
import dev.notalpha.dashloader.misc.TranslationHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigScreen extends Screen {
	private final Screen parent;
	private final TranslationHelper translations = TranslationHelper.getInstance();
	private boolean listInitialized;
	private ConfigListWidget configWidget;

	public ConfigScreen(Screen parent) {
		super(Text.of("Dashloader config"));
		this.parent = parent;
	}

	@Override
	public void init() {
		initConfigWidget();

		this.addDrawable(new TextWidget(0, 10, this.width, this.textRenderer.fontHeight / 2, Text.of(translations.get("config.title")), this.textRenderer));
		this.addDrawableChild(configWidget).update();

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.client.setScreen(this.parent)).dimensions(this.width / 2 - 154, this.height - 28, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
			this.saveConfig();
			this.client.setScreen(this.parent);
		}).dimensions(this.width / 2 + 4, this.height - 28, 150, 20).build());
	}

	private void initConfigWidget() {
		if (this.listInitialized) {
			this.configWidget.setDimensionsAndPosition(this.width, this.height - 57, 0, 24);
			return;
		}

		this.listInitialized = true;
		this.configWidget = new ConfigListWidget(this.client, this.width, this.height - 57, 24, 20);
		var list = configWidget;

		var config = ConfigHandler.INSTANCE.config;

		list.addCategory("config.category.behaviour");
		list.addIntSlider("config.compression", config.compression, 3, 0, 22, v -> config.compression = (byte) v);
		list.addIntField("config.max_caches", config.maxCaches, 5, v -> config.maxCaches = v);
		list.addBoolToggle("config.single_threaded_reading", config.singleThreadedReading, false, v -> config.singleThreadedReading = v);

		list.addCategory("config.category.visuals");
		list.addBoolToggle("config.caching_toast", config.showCachingToast, true, v -> config.showCachingToast = v);
		list.addBoolToggle("config.default_splashes", config.addDefaultSplashLines, true, v -> config.addDefaultSplashLines = v);

		var splashes = config.customSplashLines.stream().map(s -> s.replace(";", ";;")).collect(Collectors.joining(";"));

		list.addTextField("config.custom_splashes", splashes, "",
				v -> config.customSplashLines = v.isEmpty() ? List.of() : Arrays.stream(v.replace(";;", "\u0001").split(";")).map(s -> s.replace("\u0001", ";")).toList());

		list.addCategory("config.category.features");
		for (Option module : Option.values()) {
			list.addBoolToggle("config." + module.toString(), config.options.getOrDefault(module.toString(), true), true, v -> config.options.put(module.toString(), v));
		}
	}

	private void saveConfig() {
		this.configWidget.saveValues();
		ConfigHandler.INSTANCE.saveConfig();
	}
}
