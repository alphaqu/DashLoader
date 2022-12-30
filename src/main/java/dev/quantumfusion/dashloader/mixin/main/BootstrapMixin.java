package dev.quantumfusion.dashloader.mixin.main;

import dev.quantumfusion.dashloader.ProfilerHandler;
import net.minecraft.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Bootstrap.class, priority = -69)
public class BootstrapMixin {

	@Inject(method = "initialize", at = @At("HEAD"))
	private static void timeStart(CallbackInfo ci) {
		ProfilerHandler.INSTANCE.bootstrapStart = System.currentTimeMillis();
	}

	@Inject(method = "initialize", at = @At("TAIL"))
	private static void timeStop(CallbackInfo ci) {
		ProfilerHandler.INSTANCE.bootstrapEnd = System.currentTimeMillis();
	}
}
