package dev.notalpha.dashloader.mixin.option.cache.model;

import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.model.ModelModule;
import net.minecraft.client.resources.model.ModelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures the fully baked {@code Map<BlockState, BlockStateModel>} on SAVE.
 * At {@code apply} time every model is baked and sprites are attached.
 */
@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {
	@Inject(method = "apply", at = @At("HEAD"))
	private void dashloader$captureModels(ModelManager.ReloadState preparations, CallbackInfo ci) {
		ModelModule.BLOCK_STATE_MODELS.visit(CacheStatus.SAVE, map -> map.putAll(preparations.blockStateModels()));
	}
}
