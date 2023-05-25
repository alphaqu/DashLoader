package dev.notalpha.dashloader.mixin.option.cache.sprite;

import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.sprite.SpriteModule;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(SpriteLoader.class)
public final class SpriteLoaderMixin {

	@Shadow
	@Final
	private Identifier id;

	@Inject(
			method = "load(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/Identifier;ILjava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
			at = @At(value = "RETURN"),
			cancellable = true
	)
	private void dashloaderWrite(ResourceManager resourceManager, Identifier identifier, int i, Executor executor, CallbackInfoReturnable<CompletableFuture<SpriteLoader.StitchResult>> cir) {
		SpriteModule.ATLASES.visit(CacheStatus.SAVE, map -> {
			SpriteModule.ATLAS_IDS.get(CacheStatus.SAVE).put(id, identifier);
			cir.setReturnValue(cir.getReturnValue().thenApply(stitchResult -> {
				map.put(identifier, stitchResult);
				return stitchResult;
			}));
		});
	}

	@Inject(
			method = "load(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/Identifier;ILjava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void dashloaderRead(ResourceManager resourceManager, Identifier identifier, int m, Executor executor, CallbackInfoReturnable<CompletableFuture<SpriteLoader.StitchResult>> cir) {
		SpriteModule.ATLASES.visit(CacheStatus.LOAD, map -> {
			SpriteLoader.StitchResult cached = map.get(identifier);
			if (cached != null) {
				int mipLevel = cached.mipLevel();
				// Correct the executor
				CompletableFuture<Void> completableFuture = mipLevel > 0 ? CompletableFuture.runAsync(() -> cached.regions().values().forEach(sprite -> sprite.getContents().generateMipmaps(mipLevel)), executor) : CompletableFuture.completedFuture(null);
				cir.setReturnValue(CompletableFuture.completedFuture(new SpriteLoader.StitchResult(
						cached.width(),
						cached.height(),
						mipLevel,
						cached.missing(),
						cached.regions(),
						completableFuture
				)));
				cir.cancel();
			}
		});
	}
}
