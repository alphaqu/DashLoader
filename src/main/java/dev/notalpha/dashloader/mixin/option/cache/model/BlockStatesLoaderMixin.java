package dev.notalpha.dashloader.mixin.option.cache.model;

import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.model.ModelModule;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Shortcuts {@code BlockStatesLoader.load} on LOAD with cached models wrapped as
 * {@link dev.notalpha.dashloader.client.model.DashBlockStateModel.DashUnbakedGrouped},
 * so the vanilla bake pipeline runs without any JSON parsing.
 * States missing from the cache are filled with the missing model by vanilla ({@code toStateMap}).
 */
@Mixin(BlockStatesLoader.class)
public abstract class BlockStatesLoaderMixin {
	@Inject(method = "load", at = @At("HEAD"), cancellable = true)
	private static void dashloader$loadCached(ResourceManager resourceManager, Executor prepareExecutor, CallbackInfoReturnable<CompletableFuture<BlockStatesLoader.LoadedModels>> cir) {
		var cached = ModelModule.BLOCK_STATE_UNBAKED.get(CacheStatus.LOAD);
		if (cached != null && !cached.isEmpty()) {
			Map<BlockState, BlockStateModel.UnbakedGrouped> models = new HashMap<>(cached);
			cir.setReturnValue(CompletableFuture.completedFuture(new BlockStatesLoader.LoadedModels(models)));
		}
	}
}
