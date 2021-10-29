package dev.quantumfusion.dashloader.def.mixin;


import net.minecraft.Bootstrap;
import net.minecraft.client.render.entity.EntityRenderers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;

@Mixin(Bootstrap.class)
public class BootStrapMixin {

	private static Instant start;

	@Inject(method = "initialize", at = @At(value = "HEAD"), cancellable = true)
	private static void logInitialize(CallbackInfo ci) {
		start = Instant.now();
	}

	@Inject(method = "initialize", at = @At(value = "TAIL"), cancellable = true)
	private static void logInitializeEnd(CallbackInfo ci) {
		final String aDefault = EntityRenderers.DEFAULT;
	}


}
