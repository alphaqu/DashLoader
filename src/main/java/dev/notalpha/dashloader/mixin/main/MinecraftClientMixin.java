package dev.notalpha.dashloader.mixin.main;

import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.DashLoaderClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import net.minecraft.client.GameLoadCookie;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
	@Inject(method = "reloadResourcePacks(ZLnet/minecraft/client/GameLoadCookie;)Ljava/util/concurrent/CompletableFuture;", at = @At(value = "RETURN"))
	private void reloadComplete(boolean force, GameLoadCookie loadingContext, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		cir.getReturnValue().thenRun(() -> {
			// If the state is SAVE, then this will reset before the caching process can initialize from the splash screen.
			if (DashLoaderClient.CACHE.getStatus() != CacheStatus.SAVE) {
				DashLoaderClient.CACHE.reset();
			}
		});
	}
}
