package dev.quantumfusion.dashloader.def.corehook;

import dev.quantumfusion.dashloader.core.DashLoaderCore;
import dev.quantumfusion.dashloader.core.progress.ProgressHandler;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.core.util.DashUtil;
import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.api.option.ConfigHandler;
import dev.quantumfusion.dashloader.def.api.option.Option;
import dev.quantumfusion.dashloader.def.corehook.holder.*;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StepTask;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Data
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
		if (DashLoader.isRead())
			throw new RuntimeException("Tried to map data when DashDataManager is in Read mode");

		final ProgressHandler progress = DashLoaderCore.PROGRESS;
		progress.setCurrentTask("convert");

		parent.run(new StepTask("Mapping Assets", 6), (task) -> {

			final DashDataManager dataManager = DashLoader.getData();
			if (ConfigHandler.optionActive(Option.CACHE_MODEL_LOADER)) {
				progress.setCurrentTask("convert.model");
				modelData = new DashModelData(dataManager, registry, task);

				progress.setCurrentTask("convert.image");
				spriteAtlasData = new DashSpriteAtlasData(dataManager, registry, task);
			}

			if (ConfigHandler.optionActive(Option.CACHE_PARTICLE)) {
				progress.setCurrentTask("convert.particle");
				particleData = new DashParticleData(dataManager, registry, task);
			}

			if (ConfigHandler.optionActive(Option.CACHE_FONT)) {
				progress.setCurrentTask("convert.font");
				fontManagerData = new DashFontManagerData(dataManager, registry, task);
			}

			if (ConfigHandler.optionActive(Option.CACHE_SPLASH_TEXT)) {
				progress.setCurrentTask("convert.splashtext");
				splashTextData = new DashSplashTextData(dataManager);
				task.next();
			}

			if (ConfigHandler.optionActive(Option.CACHE_SHADER)) {
				progress.setCurrentTask("convert.shader");
				shaderData = new DashShaderData(dataManager, task);
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
		data.bakedModels.setCacheResultData(DashUtil.nullable(modelData, registry, DashModelData::export));
		task.next();
		data.fonts.setCacheResultData(DashUtil.nullable(fontManagerData, registry, DashFontManagerData::export));
		task.next();
		data.spriteAtlasManager.setCacheResultData(DashUtil.nullable(spriteData, Pair::getLeft));
		data.particleAtlas.setCacheResultData(DashUtil.nullable(particleData, Pair::getRight));
		data.particleSprites.setCacheResultData(DashUtil.nullable(particleData, Pair::getLeft));

		if (shaderData != null) {
			data.shaders.setCacheResultData(DashUtil.nullable(shaderData, DashShaderData::export));
			data.getReadContextData().shaderData.putAll(shaderData.shaders);
		}
		task.next();

		data.splashText.setCacheResultData(DashUtil.nullable(splashTextData, DashSplashTextData::export));
		task.next();

		if (spriteData != null) {
			for (SpriteAtlasTexture atlas : spriteData.getValue()) {
				atlasManager.addAtlas(Option.CACHE_MODEL_LOADER, atlas);
			}
		}

		if (particleData != null) {
			atlasManager.addAtlas(Option.CACHE_PARTICLE, particleData.getRight());
		}

		modelData = null;
		spriteAtlasData = null;
		fontManagerData = null;
		splashTextData = null;
	}
}
