package dev.notalpha.dashloader.mixin.main;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.DashLoaderClient;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes f3 + t reset the cache. Also makes shift + f3 + t not reset it.
 */
@Mixin(KeyboardHandler.class)
public class KeyboardMixin {
	@Inject(
			method = "handleDebugKeys",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/Minecraft;reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;"
			)
	)
	private void f3tReloadWorld(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (!event.hasShiftDown()) {
			if (DashLoaderClient.CACHE.getStatus() == CacheStatus.IDLE) {
				DashLoader.LOG.info("Clearing cache.");
				DashLoaderClient.CACHE.remove();
			}
		}
	}
}
