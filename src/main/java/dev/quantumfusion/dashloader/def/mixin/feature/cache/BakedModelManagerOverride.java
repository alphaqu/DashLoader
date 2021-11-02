package dev.quantumfusion.dashloader.def.mixin.feature.cache;

import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.api.feature.Feature;
import dev.quantumfusion.dashloader.def.fallback.DashModelLoader;
import dev.quantumfusion.dashloader.def.mixin.accessor.ModelLoaderAccessor;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.*;
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
import java.util.concurrent.atomic.AtomicInteger;

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
			final ModelLoader fastLoader = new ModelLoader(resourceManager, this.colorMap, profiler, this.mipmapLevels);
			final ModelLoaderAccessor access = (ModelLoaderAccessor) fastLoader;
			access.setSpriteAtlasManager(this.atlasManager);

			this.models = data.bakedModels.getCacheResultData();

			final int size = this.models.size();

			final Map<Identifier, UnbakedModel> modelsToBake = access.getModelsToBake();

			AtomicInteger fallback = new AtomicInteger();
			modelsToBake.forEach((identifier, unbakedModel) -> {
				if (!(unbakedModel instanceof DashModelLoader.BakedModelWrapper)) {
					this.models.put(identifier, fastLoader.bake(identifier, ModelRotation.X0_Y0));
					fallback.getAndIncrement();
				}
			});

			DashLoader.LOGGER.info("Loaded {} out of {} models with fallback system. ({}% cache coverage)", fallback.get(), size, (int)((1 - (fallback.get() / (float) size)) * 100));
			this.stateLookup = fastLoader.getStateLookup();
		}
		this.missingModel = this.models.get(ModelLoader.MISSING_ID);
		profiler.swap("cache");
		this.blockModelCache.reload();
		profiler.pop();
		profiler.endTick();
		ci.cancel();
	}

}
