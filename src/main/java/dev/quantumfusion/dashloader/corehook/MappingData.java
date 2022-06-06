package dev.quantumfusion.dashloader.corehook;

import dev.quantumfusion.dashloader.DashDataManager;
import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.api.option.ConfigHandler;
import dev.quantumfusion.dashloader.api.option.Option;
import dev.quantumfusion.dashloader.corehook.holder.*;
import dev.quantumfusion.dashloader.progress.ProgressHandler;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.util.DashUtil;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StepTask;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@DataNullable
public class MappingData {
	@Nullable
	public DashFontManagerData fontManagerData;
	@Nullable
	public DashModelData modelData;
	@Nullable
	public DashParticleData particleData;
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
			DashFontManagerData fontManagerData,
			DashModelData modelData,
			DashParticleData particleData,
			DashSplashTextData splashTextData,
			DashSpriteAtlasData spriteAtlasData,
			DashShaderData shaderData) {
		this.fontManagerData = fontManagerData;
		this.modelData = modelData;
		this.particleData = particleData;
		this.splashTextData = splashTextData;
		this.spriteAtlasData = spriteAtlasData;
		this.shaderData = shaderData;
	}


	public void map(RegistryWriter registry, StepTask parent) {
		if (DashLoader.isRead()) {
			throw new RuntimeException("Tried to map data when DashDataManager is in Read mode");
		}

		final ProgressHandler progress = DashLoader.INSTANCE.progress;
		progress.setCurrentTask("convert");

		parent.run(new StepTask("Mapping Assets", 6), (task) -> {

			final DashDataManager dataManager = DashLoader.getData();
			if (ConfigHandler.optionActive(Option.CACHE_MODEL_LOADER)) {
				progress.setCurrentTask("convert.model");
				this.modelData = new DashModelData(dataManager, registry, task);

				progress.setCurrentTask("convert.image");
				this.spriteAtlasData = new DashSpriteAtlasData(dataManager, registry, task);
			}

			if (ConfigHandler.optionActive(Option.CACHE_PARTICLE)) {
				progress.setCurrentTask("convert.particle");
				this.particleData = new DashParticleData(dataManager, registry, task);
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

			task.finish();
		});
	}

	public void export(RegistryReader registry, DashDataManager data, @Nullable Consumer<Task> taskConsumer) {
		StepTask task = new StepTask("Exporting Assets", 6);
		if (taskConsumer != null) {
			taskConsumer.accept(task);
		}

		var spriteData = DashUtil.nullable(this.spriteAtlasData, registry, DashSpriteAtlasData::export);
		task.next();

		var particleData = DashUtil.nullable(this.particleData, registry, DashParticleData::export);
		task.next();

		var atlasManager = data.getReadContextData().dashAtlasManager;
		//	data.modelStateLookup.setCacheResultData(DashUtil.nullable(blockStateData, registry, DashBlockStateData::export));
		data.bakedModels.setCacheResultData(DashUtil.nullable(this.modelData, registry, DashModelData::export));
		task.next();
		data.fonts.setCacheResultData(DashUtil.nullable(this.fontManagerData, registry, DashFontManagerData::export));
		task.next();
		data.spriteAtlasManager.setCacheResultData(DashUtil.nullable(spriteData, Pair::getLeft));
		data.particleAtlas.setCacheResultData(DashUtil.nullable(particleData, Pair::getRight));
		data.particleSprites.setCacheResultData(DashUtil.nullable(particleData, Pair::getLeft));

		if (this.shaderData != null) {
			data.shaders.setCacheResultData(DashUtil.nullable(this.shaderData, DashShaderData::export));
			data.getReadContextData().shaderData.putAll(this.shaderData.shaders);
		}
		task.next();

		data.splashText.setCacheResultData(DashUtil.nullable(this.splashTextData, DashSplashTextData::export));
		task.next();

		if (spriteData != null) {
			for (SpriteAtlasTexture atlas : spriteData.getValue()) {
				atlasManager.addAtlas(Option.CACHE_MODEL_LOADER, atlas);
			}
		}

		if (particleData != null) {
			atlasManager.addAtlas(Option.CACHE_PARTICLE, particleData.getRight());
		}

		this.modelData = null;
		this.spriteAtlasData = null;
		this.fontManagerData = null;
		this.splashTextData = null;
	}
}
