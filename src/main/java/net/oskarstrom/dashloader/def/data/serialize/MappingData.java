package net.oskarstrom.dashloader.def.data.serialize;

import com.mojang.blaze3d.platform.TextureUtil;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.api.feature.Feature;
import net.oskarstrom.dashloader.def.data.VanillaData;
import net.oskarstrom.dashloader.def.data.serialize.mapping.*;
import net.oskarstrom.dashloader.def.image.DashSpriteAtlasTextureData;
import net.oskarstrom.dashloader.def.mixin.accessor.AbstractTextureAccessor;
import net.oskarstrom.dashloader.def.mixin.accessor.SpriteAccessor;
import net.oskarstrom.dashloader.def.mixin.accessor.SpriteAtlasTextureAccessor;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MappingData {
	@Serialize(order = 0)
	@SerializeNullable
	public DashBlockStateData blockStateData;

	@Serialize(order = 1)
	@SerializeNullable
	public DashFontManagerData fontManagerData;

	@Serialize(order = 2)
	@SerializeNullable
	public DashModelData modelData;

	@Serialize(order = 3)
	@SerializeNullable
	public DashParticleData predicateData;

	@Serialize(order = 4)
	@SerializeNullable
	public DashSplashTextData splashTextData;

	@Serialize(order = 5)
	@SerializeNullable
	public DashSpriteAtlasData spriteAtlasData;

	@Serialize(order = 6)
	@SerializeNullable
	public DashShaderData shaderData;

	private List<Pair<Feature, Pair<SpriteAtlasTexture, DashSpriteAtlasTextureData>>> atlasesToRegister;


	public MappingData() {
	}

	@SuppressWarnings("unused") //active j
	public MappingData(@Deserialize("blockStateData") DashBlockStateData blockStateData,
						@Deserialize("fontManagerData") DashFontManagerData fontManagerData,
						@Deserialize("modelData") DashModelData modelData,
						@Deserialize("predicateData") DashParticleData predicateData,
						@Deserialize("splashTextData") DashSplashTextData splashTextData,
						@Deserialize("spriteAtlasData") DashSpriteAtlasData spriteAtlasData,
						@Deserialize("shaderData") DashShaderData shaderData)
	{
		this.blockStateData = blockStateData;
		this.fontManagerData = fontManagerData;
		this.modelData = modelData;
		this.predicateData = predicateData;
		this.splashTextData = splashTextData;
		this.spriteAtlasData = spriteAtlasData;
		this.shaderData = shaderData;
	}

	public void loadVanillaData(VanillaData data, DashRegistry registry, DashLoader.TaskHandler taskHandler) {
		if (Feature.MODEL_LOADER.active()) {
			taskHandler.logAndTask("Mapping Blockstates");
			blockStateData = new DashBlockStateData(data, registry, taskHandler);

			taskHandler.logAndTask("Mapping Models");
			modelData = new DashModelData(data, registry, taskHandler);

			taskHandler.logAndTask("Mapping Atlas");
			spriteAtlasData = new DashSpriteAtlasData(data, registry, taskHandler);
		}

		if (Feature.PARTICLES.active()) {
			taskHandler.logAndTask("Mapping Particles");
			predicateData = new DashParticleData(data, registry, taskHandler);
		}

		if (Feature.FONTS.active()) {
			taskHandler.logAndTask("Mapping Fonts");
			fontManagerData = new DashFontManagerData(data, registry, taskHandler);
		}

		if (Feature.SPLASH_TEXT.active()) {
			taskHandler.logAndTask("Mapping Splash Text");
			splashTextData = new DashSplashTextData(data, taskHandler);
		}

		if (Feature.SHADERS.active()) {
			taskHandler.logAndTask("Mapping Shaders");
			shaderData = new DashShaderData(data, taskHandler);
		}
	}

	public void toUndash(DashRegistry registry, VanillaData vanillaData) {
		final Pair<SpriteAtlasManager, List<SpriteAtlasTexture>> spriteData =
				DashHelper.nullable(spriteAtlasData, spriteAtlasData -> spriteAtlasData.toUndash(registry));


		final Pair<Map<Identifier, List<Sprite>>, SpriteAtlasTexture> particleData = DashHelper.nullable(this.predicateData, data -> data.toUndash(registry));
		vanillaData.loadCacheData(
				DashHelper.nullable(spriteData, Pair::getLeft),
				DashHelper.nullable(blockStateData, data -> data.toUndash(registry)),
				DashHelper.nullable(modelData, data -> data.toUndash(registry)),
				DashHelper.nullable(particleData, Pair::getLeft),
				DashHelper.nullable(fontManagerData, data -> data.toUndash(registry)),
				DashHelper.nullable(splashTextData, DashSplashTextData::toUndash),
				DashHelper.nullable(shaderData, DashShaderData::toUndash)
		);
		atlasesToRegister = new ArrayList<>();
		if (spriteData != null) {
			spriteData.getValue().forEach(atlasTexture -> atlasesToRegister.add(Pair.of(Feature.MODEL_LOADER, Pair.of(atlasTexture, vanillaData.getAtlasData(atlasTexture)))));
		}

		if (particleData != null) {
			SpriteAtlasTexture texture = particleData.getRight();
			atlasesToRegister.add(Pair.of(Feature.PARTICLES, Pair.of(texture, vanillaData.getAtlasData(texture))));
		}

		modelData = null;
		spriteAtlasData = null;
		blockStateData = null;
		fontManagerData = null;
		splashTextData = null;
	}

	public void registerAtlases(TextureManager textureManager, Feature feature) {
		atlasesToRegister.forEach((pair) -> {
			if (pair.getLeft() == feature) {
				final Pair<SpriteAtlasTexture, DashSpriteAtlasTextureData> atlas = pair.getRight();
				registerAtlas(atlas.getLeft(), atlas.getRight(), textureManager);
			}
		});
	}

	public void registerAtlas(SpriteAtlasTexture atlasTexture, DashSpriteAtlasTextureData data, TextureManager textureManager) {
		//atlas registration
		final Identifier id = atlasTexture.getId();
		final int glId = TextureUtil.generateTextureId();
		final int width = data.width;
		final int maxLevel = data.maxLevel;
		final int height = data.height;
		((AbstractTextureAccessor) atlasTexture).setGlId(glId);
		//ding dong lwjgl here are their styles

		TextureUtil.prepareImage(glId, maxLevel, width, height);
		((SpriteAtlasTextureAccessor) atlasTexture).getSprites().forEach((identifier, sprite) -> {
			final SpriteAccessor access = (SpriteAccessor) sprite;
			access.setAtlas(atlasTexture);
			access.setId(identifier);
			sprite.upload();
		});

		//helu textures here are the atlases
		textureManager.registerTexture(id, atlasTexture);
		atlasTexture.setFilter(false, maxLevel > 0);
		DashLoader.LOGGER.info("Allocated: {}x{}x{} {}-atlas", width, height, maxLevel, id);
	}

	@Nullable
	public SpriteAtlasTexture getAtlas(Identifier identifier) {
		for (Pair<Feature, Pair<SpriteAtlasTexture, DashSpriteAtlasTextureData>> pair : atlasesToRegister) {
			final SpriteAtlasTexture atlas = pair.getRight().getLeft();
			if (identifier.equals(atlas.getId())) {
				return atlas;
			}
		}
		return null;
	}
}
