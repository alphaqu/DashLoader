package dev.quantumfusion.dashloader.def.corehook;

import dev.quantumfusion.dashloader.core.DashLoaderCore;
import dev.quantumfusion.dashloader.core.progress.ProgressHandler;
import dev.quantumfusion.dashloader.core.progress.task.CountTask;
import dev.quantumfusion.dashloader.core.progress.task.Task;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.core.util.DashUtil;
import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.DashDataManager.DashReadContextData.DashAtlasManager;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.api.option.ConfigHandler;
import dev.quantumfusion.dashloader.def.api.option.Option;
import dev.quantumfusion.dashloader.def.corehook.holder.*;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

@Data
@DataNullable
public class MappingData {
	@Nullable
	public DashBlockStateData blockStateData;
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
			DashBlockStateData blockStateData,
			DashFontManagerData fontManagerData,
			DashModelData modelData,
			DashParticleData particleData,
			DashSplashTextData splashTextData,
			DashSpriteAtlasData spriteAtlasData,
			DashShaderData shaderData) {
		this.blockStateData = blockStateData;
		this.fontManagerData = fontManagerData;
		this.modelData = modelData;
		this.particleData = particleData;
		this.splashTextData = splashTextData;
		this.spriteAtlasData = spriteAtlasData;
		this.shaderData = shaderData;
	}


	public void map(RegistryWriter registry, Task task) {
		if (DashLoader.isRead())
			throw new RuntimeException("Tried to map data when DashDataManager is in Read mode");

		final ProgressHandler progress = DashLoaderCore.PROGRESS;
		progress.setCurrentTask("Mapping");
		CountTask mapping = new CountTask(7);
		task.setSubtask(mapping);


		final DashDataManager dataManager = DashLoader.getData();
		if (ConfigHandler.optionActive(Option.CACHE_MODEL_LOADER)) {

			progress.setCurrentTask("Mapping Blockstates");
			blockStateData = new DashBlockStateData(dataManager, registry);
			mapping.completedTask();

			progress.setCurrentTask("Mapping Models");
			modelData = new DashModelData(dataManager, registry);
			mapping.completedTask();

			progress.setCurrentTask("Mapping Sprites");
			spriteAtlasData = new DashSpriteAtlasData(dataManager, registry);
			mapping.completedTask();
		}

		if (ConfigHandler.optionActive(Option.CACHE_PARTICLE)) {
			progress.setCurrentTask("Mapping Particles");
			particleData = new DashParticleData(dataManager, registry);
			mapping.completedTask();
		}

		if (ConfigHandler.optionActive(Option.CACHE_FONT)) {
			progress.setCurrentTask("Mapping Fonts");
			fontManagerData = new DashFontManagerData(dataManager, registry);
			mapping.completedTask();
		}

		if (ConfigHandler.optionActive(Option.CACHE_SPLASH_TEXT)) {
			progress.setCurrentTask("Mapping SplashText");
			splashTextData = new DashSplashTextData(dataManager);
			mapping.completedTask();
		}

		if (ConfigHandler.optionActive(Option.CACHE_SHADER)) {
			progress.setCurrentTask("Mapping Shaders");
			shaderData = new DashShaderData(dataManager);
			mapping.completedTask();
		}
	}

	public void export(RegistryReader registry, DashDataManager data) {
		var spriteData = DashUtil.nullable(this.spriteAtlasData, registry, DashSpriteAtlasData::export);
		var particleData = DashUtil.nullable(this.particleData, registry, DashParticleData::export);

		var atlasManager = data.getReadContextData().dashAtlasManager;
		data.modelStateLookup.setCacheResultData(DashUtil.nullable(blockStateData, registry, DashBlockStateData::export));
		data.bakedModels.setCacheResultData(DashUtil.nullable(modelData, registry, DashModelData::export));
		data.fonts.setCacheResultData(DashUtil.nullable(fontManagerData, registry, DashFontManagerData::export));
		data.spriteAtlasManager.setCacheResultData(DashUtil.nullable(spriteData, Pair::getLeft));
		data.particleAtlas.setCacheResultData(DashUtil.nullable(particleData, Pair::getRight));
		data.particleSprites.setCacheResultData(DashUtil.nullable(particleData, Pair::getLeft));

		if (shaderData != null) {
			data.shaders.setCacheResultData(DashUtil.nullable(shaderData, DashShaderData::export));
			data.getReadContextData().shaderData.addAll(shaderData.shaders.values());
		}

		data.splashText.setCacheResultData(DashUtil.nullable(splashTextData, DashSplashTextData::export));

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
		blockStateData = null;
		fontManagerData = null;
		splashTextData = null;
	}
}
