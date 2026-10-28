package dev.notalpha.dashloader.client.ui;

import dev.notalpha.dashloader.client.ui.widget.ConfigListWidget;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.config.Option;
import dev.notalpha.dashloader.misc.TranslationHelper;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
	private final Screen parent;
	private final TranslationHelper translations = TranslationHelper.getInstance();
	private boolean listInitialized;
	private ConfigListWidget configWidget;

	public ConfigScreen(Screen parent) {
		super(Component.nullToEmpty("Dashloader config"));
		this.parent = parent;
	}

	@Override
	public void init() {
		initConfigWidget();

		this.addRenderableOnly(new StringWidget(0, 10, this.width, this.font.lineHeight / 2, Component.nullToEmpty(translations.get("config.title")), this.font));
		this.addRenderableWidget(configWidget).update();

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent)).bounds(this.width / 2 - 154, this.height - 28, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
			this.saveConfig();
			this.minecraft.setScreen(this.parent);
		}).bounds(this.width / 2 + 4, this.height - 28, 150, 20).build());
	}

	private void initConfigWidget() {
		if (this.listInitialized) {
			this.configWidget.setRectangle(this.width, this.height - 57, 0, 24);
			return;
		}

		this.listInitialized = true;
		this.configWidget = new ConfigListWidget(this.minecraft, this.width, this.height - 57, 24, 20);
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
