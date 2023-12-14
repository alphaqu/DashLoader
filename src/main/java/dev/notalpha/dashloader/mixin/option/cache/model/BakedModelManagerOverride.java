package dev.notalpha.dashloader.mixin.option.cache.model;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.model.ModelModule;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = BakedModelManager.class, priority = 69420)
public abstract class BakedModelManagerOverride {
	@Shadow
	private Map<Identifier, BakedModel> models;

	@Inject(
			method = "reloadModels",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private static void reloadModels(ResourceManager resourceManager, Executor executor, CallbackInfoReturnable<CompletableFuture<Map<Identifier, JsonUnbakedModel>>> cir) {
		ModelModule.MODELS_LOAD.visit(CacheStatus.LOAD, map -> {
			cir.setReturnValue(CompletableFuture.completedFuture(new HashMap<>(map)));
		});
	}


	@Inject(
			method = "upload",
			at = @At(value = "TAIL")
	)
	private void yankAssets(BakedModelManager.BakingResult bakingResult, Profiler profiler, CallbackInfo ci) {
		ModelModule.MODELS_SAVE.visit(CacheStatus.SAVE, map -> {
			DashLoader.LOG.info("Yanking Minecraft Assets");
			map.putAll(this.models);
		});
	}

}
