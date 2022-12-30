package dev.quantumfusion.dashloader.mixin.option.cache.model;

import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.ProfilerHandler;
import dev.quantumfusion.dashloader.minecraft.model.ModelCacheHandler;
import dev.quantumfusion.dashloader.minecraft.model.fallback.MissingDashModel;
import dev.quantumfusion.dashloader.minecraft.model.fallback.UnbakedBakedModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;

import static net.minecraft.client.render.model.ModelLoader.MISSING_ID;

@Mixin(value = ModelLoader.class, priority = 69420)
public abstract class ModelLoaderMixin {

	@Mutable
	@Shadow
	@Final
	private Map<Identifier, UnbakedModel> unbakedModels;

	@Shadow
	protected abstract void method_4716(BlockState blockState);

	@Mutable
	@Shadow
	@Final
	private Set<Identifier> modelsToLoad;

	@Mutable
	@Shadow
	@Final
	private Map<Identifier, UnbakedModel> modelsToBake;

	@Shadow
	protected abstract JsonUnbakedModel loadModelFromJson(Identifier id) throws IOException;

	@Inject(
			method = "<init>",
			at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=static_definitions", shift = At.Shift.AFTER)
	)
	private void injectLoadedModels(BlockColors blockColors, Profiler profiler, Map<Identifier, JsonUnbakedModel> jsonUnbakedModels, Map<Identifier, List<ModelLoader.SourceTrackedData>> blockStates, CallbackInfo ci) {
		ModelCacheHandler.MODELS.visit(DashLoader.Status.LOAD, dashModels -> {
			DashLoader.LOG.info("Injecting {} Cached Models", dashModels.size());
			this.unbakedModels = new HashMap<>(this.unbakedModels);
			this.modelsToBake = new HashMap<>(this.modelsToBake);
			this.modelsToLoad = new HashSet<>(this.modelsToLoad);

			HashSet<Identifier> filter = new HashSet<>(this.unbakedModels.size() + this.modelsToBake.size());
			filter.addAll(this.unbakedModels.keySet());
			filter.addAll(this.modelsToBake.keySet());
			dashModels.forEach((identifier, bakedModel) -> {
				if (!(bakedModel instanceof MissingDashModel) && !filter.contains(identifier)) {
					UnbakedBakedModel unbakedBakedModel = new UnbakedBakedModel(bakedModel, identifier);
					this.unbakedModels.put(identifier, unbakedBakedModel);
					this.modelsToBake.put(identifier, unbakedBakedModel);
				}
			});
		});
	}

	/**
	 * We want to not load all of the blockstate models as we have a list of them available on which ones to load to save a lot of computation
	 */
	@Redirect(
			method = "<init>",
			at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 0)
	)
	private boolean loadMissingModels(Iterator instance) {
		var map = ModelCacheHandler.MISSING_READ.get(DashLoader.Status.LOAD);
		if (map != null) {
			for (BlockState blockState : map.keySet()) {
				// load thing lambda
				this.method_4716(blockState);
			}
			DashLoader.LOG.info("Loaded {} unsupported models.", map.size());
			return false;
		}
		return instance.hasNext();
	}

	@Redirect(
			method = "<init>",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/ModelLoader;loadModelFromJson(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/model/json/JsonUnbakedModel;")
	)
	private JsonUnbakedModel pleaseDontLoadMissingModelBecauseItsReallySlowThankYou(ModelLoader instance, Identifier id) throws IOException {
		if (ModelCacheHandler.MODELS.active(DashLoader.Status.LOAD)) {
			return null;
		}
		return loadModelFromJson(MISSING_ID);
	}

	@Inject(
			method = "<init>",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/ModelLoader;addModel(Lnet/minecraft/client/util/ModelIdentifier;)V", ordinal = 0, shift = At.Shift.BEFORE)
	)
	private void pleaseDontLoadMissingModelBecauseItsReallySlowThankYouPart2(BlockColors blockColors, Profiler profiler, Map jsonUnbakedModels, Map blockStates, CallbackInfo ci) {
		ModelCacheHandler.MODELS.visit(DashLoader.Status.LOAD, map -> {
			this.unbakedModels.put(MISSING_ID, new UnbakedBakedModel(map.get(MISSING_ID), MISSING_ID));
		});
	}

	@Inject(
			method = "bake",
			at = @At(
					value = "HEAD"
			)
	)
	private void countModels(BiFunction<Identifier, SpriteIdentifier, Sprite> spriteLoader, CallbackInfo ci) {
		if (ModelCacheHandler.MODELS.active(DashLoader.Status.LOAD)) {
			// Cache stats
			int cachedModels = 0;
			int fallbackModels = 0;
			for (UnbakedModel value : this.modelsToBake.values()) {
				if (value instanceof UnbakedBakedModel) {
					cachedModels += 1;
				} else {
					fallbackModels += 1;
				}
			}
			ProfilerHandler.INSTANCE.cachedModelsCount = cachedModels;
			ProfilerHandler.INSTANCE.fallbackModelsCount = fallbackModels;
		}

	}
}
