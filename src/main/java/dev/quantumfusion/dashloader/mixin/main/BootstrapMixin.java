package dev.quantumfusion.dashloader.mixin.main;

import net.minecraft.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static dev.quantumfusion.dashloader.DashLoader.DL;

@Mixin(value = Bootstrap.class, priority = -69)
public class BootstrapMixin {

	@Inject(method = "initialize", at = @At("HEAD"))
	private static void timeStart(CallbackInfo ci) {
		DL.profilerHandler.bootstrap_start = System.currentTimeMillis();
	}

	@Inject(method = "initialize", at = @At("TAIL"))
	private static void timeStop(CallbackInfo ci) {
		DL.profilerHandler.bootstrap_end = System.currentTimeMillis();
	}
}
