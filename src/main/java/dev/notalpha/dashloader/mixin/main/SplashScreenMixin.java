package dev.notalpha.dashloader.mixin.main;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.cache.Cache;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.DashLoaderClient;
import dev.notalpha.dashloader.client.ui.toast.DashToast;
import dev.notalpha.dashloader.client.ui.toast.DashToastState;
import dev.notalpha.dashloader.client.ui.toast.DashToastStatus;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.misc.ProfilerUtil;
import dev.notalpha.taski.builtin.StaticTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LoadingOverlay.class, priority = 69420)
public class SplashScreenMixin {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Shadow
	private long fadeOutStart;
	@Shadow
	@Final
	private ReloadInstance reload;
	@Mutable
	@Shadow
	@Final
	private boolean fadeIn;

	@Inject(
			method = "tick",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMillis()J", shift = At.Shift.AFTER)
	)
	private void done(CallbackInfo ci) {
		this.minecraft.setOverlay(null);
		if (this.minecraft.screen != null) {
  			if (this.minecraft.screen instanceof TitleScreen) {
  				this.minecraft.screen = new TitleScreen(false);
  			}
		}

		DashLoader.LOG.info("Minecraft reloaded in {}", ProfilerUtil.getTimeStringFromStart(ProfilerUtil.RELOAD_START));
		Cache cache = DashLoaderClient.CACHE;
		if (DashLoaderClient.CACHE.getStatus() == CacheStatus.SAVE && minecraft.getToastManager().getToast(DashToast.class, Toast.NO_TOKEN) == null) {
			final DashToastState state;
			if (ConfigHandler.instance().config.showCachingToast) {
				DashToast toast = new DashToast();
				minecraft.getToastManager().addToast(toast);
				state = toast.state;
			} else {
				state = new DashToastState();
			}

			final Thread thread = new Thread(() -> {
				state.setStatus(DashToastStatus.PROGRESS);
				long start = System.currentTimeMillis();
				boolean save = cache.save(stepTask -> state.task = stepTask);
				cache.reset();

				if (save) {
					state.setOverwriteText("Created cache in " + ProfilerUtil.getTimeStringFromStart(start));
					state.setFinished(DashToastStatus.DONE);
				} else {
					// Only show toast on fail.
					minecraft.execute(() -> {
						DashToastState failed;
						if (!ConfigHandler.instance().config.showCachingToast) {
							DashToast toast = new DashToast();
							minecraft.getToastManager().addToast(toast);
							failed = toast.state;
						} else {
							failed = state;
						}
						failed.setOverwriteText("Internal error, Please check logs.");
						failed.task = new StaticTask("Crash", 0);
						failed.setFinished(DashToastStatus.CRASHED);
					});
				}
			}, "dashloader-thread");
			thread.setDaemon(true);
			thread.start();
		} else {
			cache.reset();
		}
	}
}
