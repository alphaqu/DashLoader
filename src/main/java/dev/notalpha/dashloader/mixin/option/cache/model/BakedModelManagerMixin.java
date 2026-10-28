package dev.notalpha.dashloader.mixin.option.cache.model;

import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.model.ModelModule;
import net.minecraft.client.render.model.BakedModelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures the fully baked {@code Map<BlockState, BlockStateModel>} on SAVE.
 * At {@code upload} time every model is baked and sprites are attached.
 * 1.21.9: upload(BakingResult) — single arg, no Profiler.
 */
@Mixin(BakedModelManager.class)
public abstract class BakedModelManagerMixin {
	@Inject(method = "upload", at = @At("HEAD"))
	private void dashloader$captureModels(BakedModelManager.BakingResult bakingResult, CallbackInfo ci) {
		ModelModule.BLOCK_STATE_MODELS.visit(CacheStatus.SAVE, map -> map.putAll(bakingResult.modelCache()));
	}
}
