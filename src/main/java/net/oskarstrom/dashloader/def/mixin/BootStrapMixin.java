package net.oskarstrom.dashloader.def.mixin;


import net.minecraft.Bootstrap;
import net.minecraft.client.render.entity.EntityRenderers;
import net.oskarstrom.dashloader.util.DashReport;
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
		DashReport.addTime(start, "From Bootstrap");
	}

	@Inject(method = "initialize", at = @At(value = "TAIL"), cancellable = true)
	private static void logInitializeEnd(CallbackInfo ci) {
		final String aDefault = EntityRenderers.DEFAULT;
		//noinspection EqualsWithItself, stfu intelij
		if (aDefault.equals(aDefault)) {
			DashReport.addEntry(new DashReport.Entry(start, "Bootstrap", false));
			DashReport.addTime(Instant.now(), "After Bootstrap");
		}
	}


}
