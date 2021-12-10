package dev.quantumfusion.dashloader.def.mixin.option.cache.model;

import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.api.option.ConfigHandler;
import dev.quantumfusion.dashloader.def.api.option.Option;
import dev.quantumfusion.dashloader.def.fallback.UnbakedBakedModel;
import dev.quantumfusion.dashloader.def.mixin.accessor.ModelLoaderAccessor;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.SpriteAtlasTexture;
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

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(value = BakedModelManager.class, priority = 69420)
public abstract class BakedModelManagerOverride {
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

	@Inject(method = "apply*",
			at = @At(value = "HEAD"), cancellable = true)
	private void applyStage(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
		profiler.startTick();
		profiler.push("upload");
		final DashDataManager data = DashLoader.getData();
		if (DashLoader.isWrite() || !ConfigHandler.optionActive(Option.CACHE_MODEL_LOADER)) {
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
			data.getReadContextData().dashAtlasManager.registerAtlases(textureManager, Option.CACHE_MODEL_LOADER);
			profiler.swap("baking");
			profiler.pop();


			var access = (ModelLoaderAccessor) modelLoader;

			this.atlasManager = data.spriteAtlasManager.getCacheResultData();
			this.models = data.bakedModels.getCacheResultData();
			final Map<Identifier, UnbakedModel> modelsToBake = access.getModelsToBake();
			access.setSpriteAtlasManager(this.atlasManager);

			DashLoader.LOGGER.info("Baking fallback models.");
			AtomicInteger fallback = new AtomicInteger();
			modelsToBake.forEach((identifier, unbakedModel) -> {
				if (!(unbakedModel instanceof UnbakedBakedModel)) {
					this.models.put(identifier, modelLoader.bake(identifier, ModelRotation.X0_Y0));
					if (!identifier.equals(ModelLoader.MISSING_ID)) {
						fallback.getAndIncrement();
					}
				}
			});

			final int size = this.models.size();
			DashLoader.LOGGER.info("Baked {} out of {} models with fallback system. ({}% cache coverage)", fallback.get(), size, (int) ((1 - (fallback.get() / (float) size)) * 100));
			this.stateLookup = modelLoader.getStateLookup();
		}
		this.missingModel = this.models.get(ModelLoader.MISSING_ID);
		profiler.swap("cache");
		this.blockModelCache.reload();
		profiler.pop();
		profiler.endTick();
		ci.cancel();
	}

}
