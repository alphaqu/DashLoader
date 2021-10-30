package dev.quantumfusion.dashloader.def.data;

import dev.quantumfusion.dashloader.def.data.image.DashSpriteAtlasTextureData;
import dev.quantumfusion.dashloader.def.mixin.accessor.ParticleManagerSimpleSpriteProviderAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.Font;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.MultipartBakedModel;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.stb.STBTTFontinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VanillaData {
	public final List<SpriteAtlasTexture> extraAtlases = new ArrayList<>();
	public final Map<SpriteAtlasTexture, DashSpriteAtlasTextureData> atlasData = new HashMap<>();
	public final Map<BakedModel, Pair<List<MultipartModelSelector>, StateManager<Block, BlockState>>> multipartData = new HashMap<>();
	public final Int2ObjectMap<List<String>> programData = new Int2ObjectOpenHashMap<>();
	public final Object2ObjectMap<STBTTFontinfo, Identifier> fontData = new Object2ObjectOpenHashMap<>();
	public final Object2ObjectMap<MultipartModelSelector, StateManager<Block, BlockState>> stateManagers = new Object2ObjectOpenHashMap<>();

	private SpriteAtlasManager atlasManager;
	private Object2IntMap<BlockState> stateLookup;
	private Map<Identifier, BakedModel> models;
	private Map<Identifier, List<Sprite>> particles;
	private SpriteAtlasTexture particleAtlas;
	private Map<Identifier, List<Font>> fonts;
	private List<String> splashText;
	private Map<String, Shader> shaders;


	public VanillaData() {
	}


	public void clearData() {
		extraAtlases.clear();
		atlasData.clear();
		multipartData.clear();
		programData.clear();
		stateManagers.clear();
		atlasManager = null;
		stateLookup = null;
		models = null;
		particles = null;
		particleAtlas = null;
		fonts = null;
		splashText = null;
		shaders = null;
	}

	public void loadCacheData(SpriteAtlasManager atlasManager,
							  Object2IntMap<BlockState> stateLookup,
							  Map<Identifier, BakedModel> models,
							  Map<Identifier, List<Sprite>> particles,
							  Map<Identifier, List<Font>> fonts,
							  List<String> splashText,
							  Map<String, Shader> shaders) {
		this.atlasManager = atlasManager;
		this.stateLookup = stateLookup;
		this.models = models;
		this.particles = particles;
		this.fonts = fonts;
		this.splashText = splashText;
		this.shaders = shaders;
	}


	public void addProgramData(int program, List<String> data) {
		programData.put(program, data);
	}

	public List<String> getProgramData(int program) {
		return programData.get(program);
	}


	public void setShaderAssets(Map<String, Shader> shaders) {
		this.shaders = shaders;
	}

	public void addExtraAtlasAssets(SpriteAtlasTexture atlas) {
		extraAtlases.add(atlas);
	}

	public void addAtlasData(SpriteAtlasTexture atlas, DashSpriteAtlasTextureData data) {
		atlasData.put(atlas, data);
	}

	public void addMultipartModelPredicate(MultipartBakedModel model, Pair<List<MultipartModelSelector>, StateManager<Block, BlockState>> data) {
		multipartData.put(model, data);
	}

	public void setBakedModelAssets(SpriteAtlasManager atlasManager,
									Object2IntMap<BlockState> stateLookup,
									Map<Identifier, BakedModel> models) {
		this.atlasManager = atlasManager;
		this.models = models;
		this.stateLookup = stateLookup;
	}

	public void setFontAssets(Map<Identifier, List<Font>> fonts) {
		this.fonts = fonts;
	}

	public void setParticleManagerAssets(Map<Identifier, ParticleManager.SimpleSpriteProvider> particles, SpriteAtlasTexture atlas) {
		this.particles = new HashMap<>();
		particles.forEach((identifier, simpleSpriteProvider) -> this.particles.put(identifier, ((ParticleManagerSimpleSpriteProviderAccessor) simpleSpriteProvider).getSprites()));
		particleAtlas = atlas;
	}


	public void addTypeFontAsset(STBTTFontinfo stbttFontinfo, Identifier path) {
		fontData.put(stbttFontinfo, path);
	}

	public void addPredicateStateManager(MultipartModelSelector selector, StateManager<Block, BlockState> stateStateManager) {
		stateManagers.put(selector, stateStateManager);
	}

	public Object2ObjectMap<STBTTFontinfo, Identifier> getFontData() {
		return fontData;
	}

	public void setSplashTextAssets(List<String> splashText) {
		this.splashText = splashText;
	}

	public SpriteAtlasManager getAtlasManager() {
		return atlasManager;
	}


	public Object2IntMap<BlockState> getStateLookup() {
		return stateLookup;
	}


	public Map<Identifier, BakedModel> getModels() {
		return models;
	}

	public Map<Identifier, List<Sprite>> getParticles() {
		return particles;
	}

	public Map<Identifier, List<Font>> getFonts() {
		return fonts;
	}

	public List<String> getSplashText() {
		return splashText;
	}

	public DashSpriteAtlasTextureData getAtlasData(SpriteAtlasTexture atlasTexture) {
		return atlasData.get(atlasTexture);
	}

	public Pair<List<MultipartModelSelector>, StateManager<Block, BlockState>> getModelData(BakedModel model) {
		return multipartData.get(model);
	}

	public List<SpriteAtlasTexture> getExtraAtlases() {
		return extraAtlases;
	}

	public Map<String, Shader> getShaderData() {
		return shaders;
	}

	public SpriteAtlasTexture getParticleAtlas() {
		return particleAtlas;
	}
}
