package dev.quantumfusion.dashloader.def.mixin.main;

import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.client.DashCachingScreen;
import dev.quantumfusion.dashloader.def.util.TimeUtil;
import dev.quantumfusion.dashloader.def.util.mixins.MixinThings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceReload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.management.ManagementFactory;


@Mixin(value = SplashOverlay.class, priority = 69420)
public class SplashScreenMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow private long reloadCompleteTime;

	@Shadow @Final private ResourceReload reload;

	@Mutable
	@Shadow @Final private boolean reloading;

	@Inject(
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J", shift = At.Shift.BEFORE, ordinal = 1)
	)
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		DashLoader.LOGGER.info("</> DashLoader Profiled {}", "Times"); // ij labels plz show
		this.client.setOverlay(null);
		if (DashLoader.INSTANCE.getStatus() == DashLoader.Status.READ) {
			if (client.currentScreen != null) {
				if (this.client.currentScreen instanceof TitleScreen) {
					DashLoader.LOGGER.info("</> ==> DashLoader Export time {}", TimeUtil.getTimeString(DashLoader.EXPORT_END - DashLoader.EXPORT_START));
					this.client.currentScreen = new TitleScreen(false);
				}
			}
		} else {
			this.client.currentScreen = new DashCachingScreen(this.client.currentScreen);
		}
		DashLoader.LOGGER.info("</> ==> Minecraft Reload time {}", TimeUtil.getTimeStringFromStart(DashLoader.RELOAD_START));
		DashLoader.LOGGER.info("</> ==> Minecraft Bootstrap time {}", TimeUtil.getTimeString(MixinThings.BOOTSTRAP_END - MixinThings.BOOTSTRAP_START));
		DashLoader.LOGGER.info("</> ==> Total Loading time {}", TimeUtil.getTimeString(ManagementFactory.getRuntimeMXBean().getUptime()));
	}

	@Inject(
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourceReload;isComplete()Z", shift = At.Shift.BEFORE)
	)
	private void removeMinimumTime(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (this.reloadCompleteTime == -1L && this.reload.isComplete()) {
			this.reloading = false;
		}
	}
}
