package net.oskarstrom.dashloader.def.mixin.feature.cache;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.oskarstrom.dashloader.DashLoader;
import net.oskarstrom.dashloader.DashMappings;
import net.oskarstrom.dashloader.api.feature.Feature;
import net.oskarstrom.dashloader.data.VanillaData;
import net.oskarstrom.dashloader.util.enums.DashCacheState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BakedModelManager.class)
public class BakedModelManagerOverride {

	@Shadow
	@Final
	private BlockColors colorMap;
	@Shadow
	private int mipmapLevels;
	@Shadow
	@Nullable
	private SpriteAtlasManager atlasManager;
	@Shadow
	@Final
	private TextureManager textureManager;

	@Shadow
	private Map<Identifier, BakedModel> models;

	@Shadow
	private Object2IntMap<BlockState> stateLookup;

	@Shadow
	private BakedModel missingModel;

	@Shadow
	@Final
	private BlockModels blockModelCache;

	@Inject(method = "prepare",
			at = @At(value = "HEAD"), cancellable = true)
	private void prepare(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<ModelLoader> cir) {
		profiler.startTick();
		ModelLoader modelLoader;
		if (DashLoader.getInstance().state != DashCacheState.LOADED) {
			DashLoader.LOGGER.info("DashLoader not loaded, Initializing minecraft ModelLoader to create assets for caching.");
			modelLoader = new ModelLoader(resourceManager, this.colorMap, profiler, this.mipmapLevels);
		} else {
			DashLoader.LOGGER.info("Skipping the ModelLoader as DashLoader has assets loaded.");
			//hipidy hopedy this is now dashes property
			modelLoader = null;
		}
		profiler.endTick();
		cir.setReturnValue(modelLoader);

	}

	@Inject(method = "apply",
			at = @At(value = "HEAD"), cancellable = true)
	private void applyStage(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
		profiler.startTick();
		profiler.push("upload");
		DashLoader loader = DashLoader.getInstance();
		if (loader.state != DashCacheState.LOADED) {
			//serialization
			this.atlasManager = modelLoader.upload(this.textureManager, profiler);
			this.models = modelLoader.getBakedModelMap();
			this.stateLookup = modelLoader.getStateLookup();
			DashLoader.getVanillaData().setBakedModelAssets(atlasManager, stateLookup, models);

		} else {
			//cache go brr
			DashLoader.LOGGER.info("Starting apply stage.");
			//register textures
			profiler.push("atlas");
			final DashMappings mappings = loader.getMappings();
			if (mappings != null) {
				mappings.registerAtlases(textureManager, Feature.MODEL_LOADER);
			}
			profiler.swap("baking");
			profiler.pop();
			final VanillaData vanillaData = DashLoader.getVanillaData();
			this.atlasManager = vanillaData.getAtlasManager();
			this.models = vanillaData.getModels();
			this.stateLookup = vanillaData.getStateLookup();
		}
		this.missingModel = this.models.get(ModelLoader.MISSING_ID);
		profiler.swap("cache");
		this.blockModelCache.reload();
		profiler.pop();
		profiler.endTick();
		ci.cancel();
	}

}
