package dev.notalpha.dashloader.mixin.main;

import dev.notalpha.dashloader.Cache;
import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.client.DashLoaderClient;
import dev.notalpha.dashloader.client.ui.DashToast;
import dev.notalpha.dashloader.misc.ProfilerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceReload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = SplashOverlay.class, priority = 69420)
public class SplashScreenMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	private long reloadCompleteTime;

	@Shadow
	@Final
	private ResourceReload reload;

	@Mutable
	@Shadow
	@Final
	private boolean reloading;

	@Inject(
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J", shift = At.Shift.BEFORE, ordinal = 1)
	)
	private void done(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		this.client.setOverlay(null);
		if (this.client.currentScreen != null) {
			if (this.client.currentScreen instanceof TitleScreen) {
				this.client.currentScreen = new TitleScreen(false);
			}
		}

		DashLoader.LOG.info("Minecraft reloaded in {}", ProfilerUtil.getTimeStringFromStart(ProfilerUtil.RELOAD_START));
		if (DashLoaderClient.CACHE.getStatus() == Cache.Status.SAVE && client.getToastManager().getToast(DashToast.class, Toast.TYPE) == null) {
			client.getToastManager().add(new DashToast(DashLoaderClient.CACHE));
		} else {
			DashLoaderClient.CACHE.setStatus(Cache.Status.IDLE);
		}
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
