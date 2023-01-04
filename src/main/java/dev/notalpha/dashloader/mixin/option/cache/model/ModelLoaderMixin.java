package dev.notalpha.dashloader.mixin.option.cache.model;

import dev.notalpha.dashloader.Cache;
import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.client.model.ModelModule;
import dev.notalpha.dashloader.client.model.fallback.UnbakedBakedModel;
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
	private Map<Identifier, UnbakedModel> modelsToBake;

	@Inject(
			method = "<init>",
			at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=static_definitions", shift = At.Shift.AFTER)
	)
	private void injectLoadedModels(BlockColors blockColors, Profiler profiler, Map<Identifier, JsonUnbakedModel> jsonUnbakedModels, Map<Identifier, List<ModelLoader.SourceTrackedData>> blockStates, CallbackInfo ci) {
		ModelModule.MODELS_LOAD.visit(Cache.Status.LOAD, dashModels -> {
			DashLoader.LOG.info("Injecting {} Cached Models", dashModels.size());
			Map<Identifier, UnbakedModel> oldUnbakedModels = this.unbakedModels;
			Map<Identifier, UnbakedModel> oldModelsToBake = this.modelsToBake;
			this.unbakedModels = new HashMap<>((int) ((oldUnbakedModels.size() + dashModels.size()) / 0.75));
			this.modelsToBake = new HashMap<>((int) ((oldModelsToBake.size() + dashModels.size()) / 0.75));

			this.unbakedModels.putAll(dashModels);
			this.unbakedModels.putAll(oldUnbakedModels);
			this.modelsToBake.putAll(dashModels);
			this.modelsToBake.putAll(oldModelsToBake);
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
		var map = ModelModule.MISSING_READ.get(Cache.Status.LOAD);
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

	@Inject(
			method = "bake",
			at = @At(
					value = "HEAD"
			)
	)
	private void countModels(BiFunction<Identifier, SpriteIdentifier, Sprite> spriteLoader, CallbackInfo ci) {
		if (ModelModule.MODELS_LOAD.active(Cache.Status.LOAD)) {
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
			long totalModels = cachedModels + fallbackModels;
			DashLoader.LOG.info("{}% Cache coverage", (int) (((cachedModels / (float) totalModels) * 100)));
			DashLoader.LOG.info("with {} Fallback models", fallbackModels);
			DashLoader.LOG.info("and  {} Cached models", cachedModels);
		}

	}
}
