package dev.notalpha.dashloader.mixin.option.cache;

import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.splash.SplashModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

@Mixin(SplashManager.class)
public class SplashTextResourceSupplierMixin {
	@Inject(
			method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/List;",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void applySplashCache(ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfoReturnable<List<Component>> cir) {
		SplashModule.TEXTS.visit(CacheStatus.LOAD, strings -> cir.setReturnValue(strings.stream().map(s -> (Component) Component.literal(s)).toList()));
	}

	@Inject(
			method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/List;",
			at = @At(value = "RETURN")
	)
	private void stealSplashCache(ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfoReturnable<List<Component>> cir) {
		SplashModule.TEXTS.visit(CacheStatus.SAVE, strings -> {
			strings.clear();
			for (Component component : cir.getReturnValue()) {
				strings.add(component.getString());
			}
		});
	}
}
