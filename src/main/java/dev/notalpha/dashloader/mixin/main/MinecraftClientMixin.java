package dev.notalpha.dashloader.mixin.main;

import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.DashLoaderClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
	@Shadow
	protected abstract void runTick(boolean tick);


	@Inject(method = "reloadResourcePacks(ZLnet/minecraft/client/Minecraft$GameLoadCookie;)Ljava/util/concurrent/CompletableFuture;", at = @At(value = "RETURN"))
	private void reloadComplete(boolean force, Minecraft.GameLoadCookie loadingContext, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		cir.getReturnValue().thenRun(() -> {
			// If the state is SAVE, then this will reset before the caching process can initialize from the splash screen.
			if (DashLoaderClient.CACHE.getStatus() != CacheStatus.SAVE) {
				DashLoaderClient.CACHE.reset();
			}
		});
	}
}
