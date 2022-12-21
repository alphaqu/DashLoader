package dev.quantumfusion.dashloader.data;

import dev.quantumfusion.dashloader.DashDataManager;
import dev.quantumfusion.dashloader.ProgressHandler;
import dev.quantumfusion.dashloader.api.option.Option;
import dev.quantumfusion.dashloader.config.ConfigHandler;
import dev.quantumfusion.dashloader.data.mapping.*;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.util.DashUtil;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StepTask;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static dev.quantumfusion.dashloader.DashLoader.DL;

@DataNullable
public class MappingData {
	@Nullable
	public DashFontManagerData fontManagerData;
	@Nullable
	public DashModelData modelData;
	@Nullable
	public DashSplashTextData splashTextData;
	@Nullable
	public DashSpriteAtlasData spriteAtlasData;
	@Nullable
	public DashShaderData shaderData;

	public MappingData() {
	}

	@SuppressWarnings("unused") // hyphen
	public MappingData(
			@Nullable DashFontManagerData fontManagerData,
			@Nullable DashModelData modelData,
			@Nullable DashSplashTextData splashTextData,
			@Nullable DashSpriteAtlasData spriteAtlasData,
			@Nullable DashShaderData shaderData) {
		this.fontManagerData = fontManagerData;
		this.modelData = modelData;
		this.splashTextData = splashTextData;
		this.spriteAtlasData = spriteAtlasData;
		this.shaderData = shaderData;
	}


	public void map(RegistryWriter registry, StepTask task) {
		if (DL.isRead()) {
			throw new RuntimeException("Tried to map data when DashDataManager is in Read mode");
		}

		final ProgressHandler progress = DL.progress;
		progress.setCurrentTask("convert");

		final DashDataManager dataManager = DL.getData();
		if (ConfigHandler.optionActive(Option.CACHE_MODEL_LOADER)) {
			progress.setCurrentTask("convert.model");
			this.modelData = new DashModelData(dataManager, registry, task);

			progress.setCurrentTask("convert.image");
			this.spriteAtlasData = new DashSpriteAtlasData(dataManager, registry, task);
		}

		if (ConfigHandler.optionActive(Option.CACHE_FONT)) {
			progress.setCurrentTask("convert.font");
			this.fontManagerData = new DashFontManagerData(dataManager, registry, task);
		}

		if (ConfigHandler.optionActive(Option.CACHE_SPLASH_TEXT)) {
			progress.setCurrentTask("convert.splashtext");
			this.splashTextData = new DashSplashTextData(dataManager);
			task.next();
		}

		if (ConfigHandler.optionActive(Option.CACHE_SHADER)) {
			progress.setCurrentTask("convert.shader");
			this.shaderData = new DashShaderData(dataManager, task);
		}

	}

	public void export(RegistryReader reader, DashDataManager data, @Nullable Consumer<Task> taskConsumer) {
		StepTask task = new StepTask("Exporting Assets", 5);
		if (taskConsumer != null) {
			taskConsumer.accept(task);
		}

		// Models
		data.bakedModels.setCacheResultData(DashUtil.nullable(this.modelData, reader, DashModelData::export));
		task.next();

		// Sprite
		if (this.spriteAtlasData != null) {
			this.spriteAtlasData.export(data, reader);
		}
		task.next();

		// Fonts
		data.fonts.setCacheResultData(DashUtil.nullable(this.fontManagerData, reader, DashFontManagerData::export));
		task.next();

		// Shaders
		if (this.shaderData != null) {
			data.shaders.setCacheResultData(DashUtil.nullable(this.shaderData, DashShaderData::export));
			data.getReadContextData().shaderData.putAll(this.shaderData.shaders);
		}
		task.next();

		// Splash
		data.splashText.setCacheResultData(DashUtil.nullable(this.splashTextData, DashSplashTextData::export));
		task.next();

		this.modelData = null;
		this.spriteAtlasData = null;
		this.fontManagerData = null;
		this.shaderData = null;
		this.splashTextData = null;
	}
}
