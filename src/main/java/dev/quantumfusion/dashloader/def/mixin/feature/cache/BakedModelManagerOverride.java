package dev.quantumfusion.dashloader.def.mixin.feature.cache;

import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.api.feature.Feature;
import dev.quantumfusion.dashloader.def.fallback.DashModelLoader;
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

	@Inject(
			method = "prepare",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void prepare(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<ModelLoader> cir) {
		profiler.startTick();
		ModelLoader modelLoader;
		if (DashLoader.isWrite()) {
			DashLoader.LOGGER.info("DashLoader not loaded, Initializing minecraft ModelLoader to create assets for caching.");
			modelLoader = new ModelLoader(resourceManager, this.colorMap, profiler, this.mipmapLevels);
			DashLoader.getData().getWriteContextData().loader = modelLoader;
		} else {
			DashLoader.LOGGER.info("Skipping the ModelLoader as DashLoader has assets loaded.");
			//hipidy hopedy this is now dashes property
			modelLoader = null;
		}
		profiler.endTick();
		cir.setReturnValue(modelLoader);

	}

	@Inject(method = "apply*",
			at = @At(value = "HEAD"), cancellable = true)
	private void applyStage(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
		profiler.startTick();
		profiler.push("upload");
		final DashDataManager data = DashLoader.getData();
		if (DashLoader.isWrite()) {
			//serialization
			this.atlasManager = modelLoader.upload(this.textureManager, profiler);
			this.models = modelLoader.getBakedModelMap();
			this.stateLookup = modelLoader.getStateLookup();

			data.spriteAtlasManager.setMinecraftData(atlasManager);
			data.bakedModels.setMinecraftData(models);
			data.modelStateLookup.setMinecraftData(stateLookup);
		} else {
			//cache go brr
			DashLoader.LOGGER.info("Starting apply stage.");
			//register textures
			profiler.push("atlas");
			data.getReadContextData().dashAtlasManager.registerAtlases(textureManager, Feature.MODEL_LOADER);
			profiler.swap("baking");
			profiler.pop();

			this.atlasManager = data.spriteAtlasManager.getCacheResultData();
			this.models = data.bakedModels.getCacheResultData();
			this.stateLookup = data.modelStateLookup.getCacheResultData();

			DashModelLoader.bakeUnsupportedModels(resourceManager, this.atlasManager, this.colorMap, this.models);
		}
		this.missingModel = this.models.get(ModelLoader.MISSING_ID);
		profiler.swap("cache");
		this.blockModelCache.reload();
		profiler.pop();
		profiler.endTick();
		ci.cancel();
	}

}
