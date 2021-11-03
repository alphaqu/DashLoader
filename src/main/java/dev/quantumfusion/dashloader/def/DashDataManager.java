package dev.quantumfusion.dashloader.def;

import com.mojang.blaze3d.platform.TextureUtil;
import dev.quantumfusion.dashloader.def.api.option.Option;
import dev.quantumfusion.dashloader.def.data.image.DashSpriteAtlasTextureData;
import dev.quantumfusion.dashloader.def.data.image.shader.DashShader;
import dev.quantumfusion.dashloader.def.fallback.DashMissingDashModel;
import dev.quantumfusion.dashloader.def.mixin.accessor.AbstractTextureAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAtlasTextureAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.Font;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBTTFontinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Our interface for giving vanilla back its data and saving vanilla data for caching.
 */
public class DashDataManager {
	private final DashWriteContextData writeContextData;
	private final DashReadContextData readContextData;

	// ModelLoader related
	public final DashDataHandler<Object2IntMap<BlockState>> modelStateLookup = new DashDataHandler<>();
	public final DashDataHandler<Map<Identifier, BakedModel>> bakedModels = new DashDataHandler<>();

	// Font related
	public final DashDataHandler<Map<Identifier, List<Font>>> fonts = new DashDataHandler<>();

	// Image related
	public final DashDataHandler<SpriteAtlasManager> spriteAtlasManager = new DashDataHandler<>();
	public final DashDataHandler<SpriteAtlasTexture> particleAtlas = new DashDataHandler<>();
	public final DashDataHandler<Map<Identifier, List<Sprite>>> particleSprites = new DashDataHandler<>();
	public final DashDataHandler<Map<String, Shader>> shaders = new DashDataHandler<>();

	// Haha related
	public final DashDataHandler<List<String>> splashText = new DashDataHandler<>();

	DashDataManager(DashWriteContextData writeContextData) {
		DashLoader.LOGGER.info("Created WRITE data manager");
		if (!DashLoader.isWrite())
			throw new RuntimeException("Wrong mode " + DashLoader.INSTANCE.getStatus() + " for WRITE data manager");
		this.writeContextData = writeContextData;
		this.readContextData = null;
	}

	DashDataManager(DashReadContextData readContextData) {
		DashLoader.LOGGER.info("Created READ data manager");
		if (!DashLoader.isRead())
			throw new RuntimeException("Wrong mode " + DashLoader.INSTANCE.getStatus() + " for WRITE data manager");
		this.writeContextData = null;
		this.readContextData = readContextData;
	}


	public DashWriteContextData getWriteContextData() {
		if (DashLoader.isRead()) throw new RuntimeException("Tried to get DashWriteContextData on read");
		return writeContextData;
	}

	public DashReadContextData getReadContextData() {
		if (DashLoader.isWrite()) throw new RuntimeException("Tried to get DashReadContextData on write");
		return readContextData;
	}

	/**
	 * For anything on write that is needed
	 */
	public static class DashWriteContextData {
		// Font related
		public final Map<STBTTFontinfo, Identifier> fontData = new HashMap<>();
		// Shader related
		public final Int2ObjectMap<List<String>> programData = new Int2ObjectOpenHashMap<>();

		// Model related
		public final Map<BakedModel, DashMissingDashModel> missingModelsWrite = new HashMap<>();
		public final Map<BakedModel, Pair<List<MultipartModelSelector>, StateManager<Block, BlockState>>> multipartPredicates = new HashMap<>();
		public final Map<MultipartModelSelector, StateManager<Block, BlockState>> stateManagers = new HashMap<>();

		// Atlas related SAME THING IN READ
		public final List<SpriteAtlasTexture> extraAtlases = new ArrayList<>();
		public final Map<SpriteAtlasTexture, DashSpriteAtlasTextureData> atlasData = new HashMap<>();

		public DashWriteContextData() {
		}
	}

	/**
	 * For anything on read that is needed
	 */
	public static class DashReadContextData {
		// Model related
		public final List<Identifier> missingModelsRead = new ArrayList<>();

		// Atlas related SAME THING IN WRITE
		public final List<SpriteAtlasTexture> extraAtlases = new ArrayList<>();
		public final Map<SpriteAtlasTexture, DashSpriteAtlasTextureData> atlasData = new HashMap<>();

		// Atlas related unique
		public final DashAtlasManager dashAtlasManager;

		// Shader related
		public final List<DashShader> shaderData;

		public DashReadContextData() {
			this.dashAtlasManager = new DashAtlasManager();
			this.shaderData = new ArrayList<>();
		}

		public class DashAtlasManager {
			private final List<Pair<Option, Pair<SpriteAtlasTexture, DashSpriteAtlasTextureData>>> atlasesToRegister;

			public DashAtlasManager() {
				this.atlasesToRegister = new ArrayList<>();
			}

			public void addAtlas(Option feature, SpriteAtlasTexture atlas) {
				var atlasData = Pair.of(atlas, DashReadContextData.this.atlasData.get(atlas));
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
				final int glId = TextureUtil.generateTextureId();
				final int width = data.width();
				final int maxLevel = data.maxLevel();
				final int height = data.height();
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


		}
	}


	public static class DashDataHandler<O> {
		@Nullable
		private O data;

		public DashDataHandler() {
			this.data = null;
		}

		public boolean dataAvailable() {
			return data != null;
		}

		public O getCacheResultData() {
			if (!DashLoader.isRead()) throw new RuntimeException("Trying to get cache-loaded data on write.");
			return data;
		}

		public void setCacheResultData(O data) {
			if (!DashLoader.isRead()) throw new RuntimeException("Trying to set mc-loaded data on read.");
			this.data = data;

		}

		public void setMinecraftData(O data) {
			if (!DashLoader.isWrite()) throw new RuntimeException("Trying to set mc-loaded data on read.");
			this.data = data;

		}

		public O getMinecraftData() {
			if (!DashLoader.isWrite()) throw new RuntimeException("Trying to get cache-loaded data on write.");
			return data;
		}

	}
}
