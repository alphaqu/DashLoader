package dev.quantumfusion.dashloader.def;

import com.mojang.blaze3d.platform.TextureUtil;
import dev.quantumfusion.dashloader.def.api.option.Option;
import dev.quantumfusion.dashloader.def.data.image.DashSpriteAtlasTextureData;
import dev.quantumfusion.dashloader.def.mixin.accessor.AbstractTextureAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAtlasTextureAccessor;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashAtlasManager {
	private final DashDataManager.DashReadContextData readContextData;
	private final List<Pair<Option, Pair<SpriteAtlasTexture, DashSpriteAtlasTextureData>>> atlasesToRegister;

	public DashAtlasManager(DashDataManager.DashReadContextData readContextData) {
		this.readContextData = readContextData;
		this.atlasesToRegister = new ArrayList<>();
	}

	public void addAtlas(Option feature, SpriteAtlasTexture atlas) {
		var atlasData = Pair.of(atlas, readContextData.atlasData.get(atlas));
		atlasesToRegister.add(Pair.of(feature, atlasData));
	}

	public void registerAtlases(TextureManager textureManager, Option feature) {
		atlasesToRegister.forEach((pair) -> {
			if (pair.getLeft() == feature) {
				final Pair<SpriteAtlasTexture, DashSpriteAtlasTextureData> atlas = pair.getRight();
				registerAtlas(atlas.getLeft(), atlas.getRight(), textureManager);
			}
		});
	}

	@Nullable
	public SpriteAtlasTexture getAtlas(Identifier identifier) {
		for (Pair<Option, Pair<SpriteAtlasTexture, DashSpriteAtlasTextureData>> pair : atlasesToRegister) {
			final SpriteAtlasTexture atlas = pair.getRight().getLeft();
			if (identifier.equals(atlas.getId())) {
				return atlas;
			}
		}
		return null;
	}

	public void registerAtlas(SpriteAtlasTexture atlasTexture, DashSpriteAtlasTextureData data, TextureManager textureManager) {
		//atlas registration
		final Identifier id = atlasTexture.getId();
		final int width = data.width();
		final int maxLevel = data.maxLevel();
		final int height = data.height();
		((AbstractTextureAccessor) atlasTexture).setGlId(-1);
		//ding dong lwjgl here are their styles

		TextureUtil.prepareImage(atlasTexture.getGlId(), maxLevel, width, height);
		final Map<Identifier, Sprite> sprites = ((SpriteAtlasTextureAccessor) atlasTexture).getSprites();
		sprites.forEach((identifier, sprite) -> {
			final SpriteAccessor access = (SpriteAccessor) sprite;
			access.setAtlas(atlasTexture);
			access.setId(identifier);
		});

		for (Sprite value : sprites.values()) {
			value.upload();
		}

		//helu textures here are the atlases
		textureManager.registerTexture(id, atlasTexture);
		atlasTexture.setFilter(false, maxLevel > 0);
		if (FabricLoader.getInstance().isModLoaded("fabric-textures-v0"))  {
			var registry = new ClientSpriteRegistryCallback.Registry(new HashMap<>(), (javaSucks) -> {});
			ClientSpriteRegistryCallback.event(id).invoker().registerSprites(atlasTexture, registry);
		}
		DashLoader.LOGGER.info("Allocated: {}x{}x{} {}-atlas", width, height, maxLevel, id);
	}


}
