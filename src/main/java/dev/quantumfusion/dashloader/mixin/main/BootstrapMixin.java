package dev.quantumfusion.dashloader.mixin.main;

import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.util.TimeUtil;
import dev.quantumfusion.dashloader.util.mixins.MixinThings;
import net.minecraft.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Bootstrap.class, priority = -69)
public class BootstrapMixin {

	@Inject(method = "initialize", at = @At("HEAD"))
	private static void timeStart(CallbackInfo ci) {
		DashLoader.LOGGER.info("Bootstrap start");
		MixinThings.BOOTSTRAP_START = System.currentTimeMillis();
	}

	@Inject(method = "initialize", at = @At("TAIL"))
	private static void timeStop(CallbackInfo ci) {
		MixinThings.BOOTSTRAP_END = System.currentTimeMillis();
		DashLoader.LOGGER.info("Bootstrap in {}", TimeUtil.getTimeString(MixinThings.BOOTSTRAP_END - MixinThings.BOOTSTRAP_START));
	}
}
