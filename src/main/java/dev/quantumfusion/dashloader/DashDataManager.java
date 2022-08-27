package dev.quantumfusion.dashloader;

import dev.quantumfusion.dashloader.data.image.DashSpriteAtlasTextureData;
import dev.quantumfusion.dashloader.data.image.shader.DashShader;
import dev.quantumfusion.dashloader.fallback.model.DashMissingDashModel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.Font;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBTTFontinfo;
import static dev.quantumfusion.dashloader.DashLoader.DL;

/**
 * Our interface for giving vanilla back its data and saving vanilla data for caching.
 */
public class DashDataManager {
	private final DashWriteContextData writeContextData;
	private final DashReadContextData readContextData;

	// ModelLoader related
	public final DashDataHandler<Map<Identifier, BakedModel>> bakedModels = new DashDataHandler<>();

	// Font related
	public final DashDataHandler<Map<Identifier, List<Font>>> fonts = new DashDataHandler<>();

	// Image related
	public final DashDataHandler<SpriteAtlasManager> spriteAtlasManager = new DashDataHandler<>();
	public final DashDataHandler<Map<String, Shader>> shaders = new DashDataHandler<>();

	// Haha related
	public final DashDataHandler<List<String>> splashText = new DashDataHandler<>();

	DashDataManager(DashWriteContextData writeContextData) {
		DL.log.info("Created WRITE data manager");
		if (DL.getStatus() != DashLoader.Status.WRITE) {
			throw new RuntimeException("Wrong mode " + DL.getStatus() + " for WRITE data manager");
		}
		this.writeContextData = writeContextData;
		this.readContextData = null;
	}

	DashDataManager(DashReadContextData readContextData) {
		DL.log.info("Created READ data manager");
		if (DL.getStatus() != DashLoader.Status.READ) {
			throw new RuntimeException("Wrong mode " + DL.getStatus() + " for WRITE data manager");
		}
		this.writeContextData = null;
		this.readContextData = readContextData;
	}


	public DashWriteContextData getWriteContextData() {
		if (DL.isRead()) {
			throw new RuntimeException("Tried to get DashWriteContextData on read");
		}
		return this.writeContextData;
	}

	public DashReadContextData getReadContextData() {
		if (DL.isWrite()) {
			throw new RuntimeException("Tried to get DashReadContextData on write");
		}
		return this.readContextData;
	}

	/**
	 * For anything on write that is needed
	 */
	public static class DashWriteContextData {
		// Font related
		public final Object2ObjectMap<STBTTFontinfo, Identifier> fontData = new Object2ObjectOpenHashMap<>();
		// Shader related
		public final Int2ObjectMap<List<String>> programData = new Int2ObjectOpenHashMap<>();

		// Model related
		public final Object2ObjectMap<BakedModel, DashMissingDashModel> missingModelsWrite = new Object2ObjectOpenHashMap<>();
		public final Object2ObjectMap<BakedModel, Pair<List<MultipartModelSelector>, StateManager<Block, BlockState>>> multipartPredicates = new Object2ObjectOpenHashMap<>();
		public final Object2ObjectMap<MultipartModelSelector, StateManager<Block, BlockState>> stateManagers = new Object2ObjectOpenHashMap<>();

		// Atlas related SAME THING IN READ
		public final List<SpriteAtlasTexture> extraAtlases = new ArrayList<>();
		public final Object2ObjectMap<SpriteAtlasTexture, DashSpriteAtlasTextureData> atlasData = new Object2ObjectOpenHashMap<>();

		public DashWriteContextData() {
		}
	}

	/**
	 * For anything on read that is needed
	 */
	public static class DashReadContextData {
		// Model related
		public final Object2ObjectMap<BlockState, Identifier> missingModelsRead = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());

		// Atlas related SAME THING IN WRITE
		public final Object2ObjectMap<SpriteAtlasTexture, DashSpriteAtlasTextureData> atlasData = new Object2ObjectOpenHashMap<>();

		// Atlas related unique
		public final DashAtlasManager dashAtlasManager;

		// Shader related
		public final Map<String, DashShader> shaderData;

		public DashReadContextData() {
			this.dashAtlasManager = new DashAtlasManager(this);
			this.shaderData = new Object2ObjectOpenHashMap<>();
		}

	}


	public static class DashDataHandler<O> {
		@Nullable
		private O data;

		public DashDataHandler() {
			this.data = null;
		}

		public boolean dataAvailable() {
			return this.data != null;
		}

		public O getCacheResultData() {
			if (DL.getStatus() != DashLoader.Status.READ) {
				throw new RuntimeException("Invalid status.");
			}
			return this.data;
		}

		public void setCacheResultData(O data) {
			if (DL.getStatus() != DashLoader.Status.READ) {
				throw new RuntimeException("Invalid status.");
			}
			this.data = data;

		}

		public void setMinecraftData(O data) {
			if (DL.getStatus() != DashLoader.Status.WRITE) {
				throw new RuntimeException("Invalid status.");
			}
			this.data = data;

		}

		public O getMinecraftData() {
			if (DL.getStatus() != DashLoader.Status.WRITE) {
				throw new RuntimeException("Invalid status.");
			}
			return this.data;
		}

	}

}
