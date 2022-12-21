package dev.quantumfusion.dashloader.mixin.option.cache;

import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static dev.quantumfusion.dashloader.DashLoader.DL;

@Mixin(SplashTextResourceSupplier.class)
public class SplashTextResourceSupplierMixin {
	@Inject(
			method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Ljava/util/List;",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void applySplashCache(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<List<String>> cir) {
		if (DL.isRead()) {
			var data = DL.getData().splashText;
			if (data.dataAvailable()) {
				cir.setReturnValue(data.getCacheResultData());
			}
		}
	}


	@Inject(
			method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Ljava/util/List;",
			at = @At(value = "RETURN")
	)
	private void stealSplashCache(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<List<String>> cir) {
		if (DL.isWrite()) {
			DL.getData().splashText.setMinecraftData(cir.getReturnValue());
		}
	}
}
