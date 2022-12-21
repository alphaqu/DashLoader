package dev.quantumfusion.dashloader.mixin.option.cache.model;

import com.mojang.datafixers.util.Pair;
import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.api.option.Option;
import dev.quantumfusion.dashloader.fallback.model.MissingDashModel;
import dev.quantumfusion.dashloader.fallback.model.UnbakedBakedModel;
import dev.quantumfusion.dashloader.util.mixins.SpriteAtlasTextureDuck;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import static dev.quantumfusion.dashloader.DashLoader.DL;

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

	@Inject(
			method = "<init>",
			at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=static_definitions", shift = At.Shift.AFTER)
	)
	private void injectLoadedModels(BlockColors blockColors, Profiler profiler, Map<Identifier, JsonUnbakedModel> jsonUnbakedModels, Map<Identifier, List<ModelLoader.SourceTrackedData>> blockStates, CallbackInfo ci) {
		if (DL.isRead()) {
			var data = DL.getData();
			var dashModels = data.bakedModels.getCacheResultData();
			DashLoader.LOG.info("Injecting {} Cached Models", dashModels.size());
			this.unbakedModels = new Object2ObjectOpenHashMap<>(this.unbakedModels);
			this.modelsToBake = new Object2ObjectOpenHashMap<>(this.modelsToBake);
			this.modelsToLoad = new ObjectOpenHashSet<>(this.modelsToLoad);

			ObjectOpenHashSet<Identifier> filter = new ObjectOpenHashSet<>(this.unbakedModels.size() + this.modelsToBake.size());
			filter.addAll(this.unbakedModels.keySet());
			filter.addAll(this.modelsToBake.keySet());
			dashModels.forEach((identifier, bakedModel) -> {
				if (!(bakedModel instanceof MissingDashModel) && !filter.contains(identifier)) {
					UnbakedBakedModel unbakedBakedModel = new UnbakedBakedModel(bakedModel, identifier);
					this.unbakedModels.put(identifier, unbakedBakedModel);
					this.modelsToBake.put(identifier, unbakedBakedModel);
				}
			});
		}
	}

	/**
	 * We want to not load all of the blockstate models as we have a list of them available on which ones to load to save a lot of computation
	 */
	@Redirect(
			method = "<init>",
			at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 0)
	)
	private boolean loadMissingModels(Iterator instance) {
		if (DL.isRead()) {
			final Object2ObjectMap<BlockState, Identifier> missingModelsRead = DL.getData().getReadContextData().missingModelsRead;
			for (BlockState blockState : missingModelsRead.keySet()) {
				// load thing lambda
				this.method_4716(blockState);
			}
			DashLoader.LOG.info("Loaded {} unsupported models.", missingModelsRead.size());
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
		if (DL.isRead()) {
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
			DL.profilerHandler.cached_models_count = cachedModels;
			DL.profilerHandler.fallback_models_count = fallbackModels;
		}
	}
}
