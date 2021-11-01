package dev.quantumfusion.dashloader.def.corehook;

import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.core.ui.DashLoaderProgress;
import dev.quantumfusion.dashloader.core.util.DashUtil;
import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.api.feature.Feature;
import dev.quantumfusion.dashloader.def.corehook.holder.*;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.apache.commons.lang3.tuple.Pair;

@Data
@DataNullable
public class MappingData {
	public DashBlockStateData blockStateData;
	public DashFontManagerData fontManagerData;
	public DashModelData modelData;
	public DashParticleData particleData;
	public DashSplashTextData splashTextData;
	public DashSpriteAtlasData spriteAtlasData;
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


	public void map(DashRegistryWriter registry) {
		if (DashLoader.isRead())
			throw new RuntimeException("Tried to map data when DashDataManager is in Read mode");

		DashLoaderProgress.PROGRESS.setCurrentSubtask("Mapping", 5);
		final DashDataManager dataManager = DashLoader.getData();

		if (Feature.MODEL_LOADER.active()) {
			blockStateData = new DashBlockStateData(dataManager, registry);
			modelData = new DashModelData(dataManager, registry);
			spriteAtlasData = new DashSpriteAtlasData(dataManager, registry);


		} DashLoaderProgress.PROGRESS.completedSubTask();

		if (Feature.PARTICLES.active()) {
			particleData = new DashParticleData(dataManager, registry);
		} DashLoaderProgress.PROGRESS.completedSubTask();

		if (Feature.FONTS.active()) {
			fontManagerData = new DashFontManagerData(dataManager, registry);
		} DashLoaderProgress.PROGRESS.completedSubTask();

		if (Feature.SPLASH_TEXT.active()) {
			splashTextData = new DashSplashTextData(dataManager);
		} DashLoaderProgress.PROGRESS.completedSubTask();

		if (Feature.SHADERS.active()) {
			shaderData = new DashShaderData(dataManager);
		} DashLoaderProgress.PROGRESS.completedSubTask();
	}

	public void export(DashRegistryReader registry, DashDataManager data) {
		var spriteData = DashUtil.nullable(this.spriteAtlasData, registry, DashSpriteAtlasData::export);
		var particleData = DashUtil.nullable(this.particleData, registry, DashParticleData::export);

		var atlasManager = data.getReadContextData().dashAtlasManager;
		data.modelStateLookup.setCacheResultData(DashUtil.nullable(blockStateData, registry, DashBlockStateData::export));
		data.bakedModels.setCacheResultData(DashUtil.nullable(modelData, registry, DashModelData::export));
		data.fonts.setCacheResultData(DashUtil.nullable(fontManagerData, registry, DashFontManagerData::export));
		data.spriteAtlasManager.setCacheResultData(DashUtil.nullable(spriteData, Pair::getLeft));
		data.particleAtlas.setCacheResultData(DashUtil.nullable(particleData, Pair::getRight));
		data.particleSprites.setCacheResultData(DashUtil.nullable(particleData, Pair::getLeft));
		data.shaders.setCacheResultData(DashUtil.nullable(shaderData, DashShaderData::export));
		data.splashText.setCacheResultData(DashUtil.nullable(splashTextData, DashSplashTextData::export));

		data.getReadContextData().shaderData.addAll(shaderData.shaders.values());
		if (spriteData != null) {
			for (SpriteAtlasTexture atlas : spriteData.getValue()) {
				atlasManager.addAtlas(Feature.MODEL_LOADER, atlas);
			}
		}

		if (particleData != null) {
			atlasManager.addAtlas(Feature.PARTICLES, particleData.getRight());
		}

		modelData = null;
		spriteAtlasData = null;
		blockStateData = null;
		fontManagerData = null;
		splashTextData = null;
	}
}
