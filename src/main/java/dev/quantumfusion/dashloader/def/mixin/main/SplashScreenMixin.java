package dev.quantumfusion.dashloader.def.mixin.main;

import dev.quantumfusion.dashloader.core.DashLoaderCore;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.client.DashCachingScreen;
import dev.quantumfusion.dashloader.def.util.TimeUtil;
import dev.quantumfusion.dashloader.def.util.mixins.MixinThings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.management.ManagementFactory;
import java.util.HashMap;

import static dev.quantumfusion.dashloader.def.DashLoader.*;


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
		client.setOverlay(null);
		if (client.currentScreen != null) {
			if (client.currentScreen instanceof TitleScreen) {
				client.currentScreen = new TitleScreen(false);
			}
		}

		LOGGER.info("┏ DashLoader Statistics.");
		if (EXPORT_TIME != -1) {
			LOGGER.info("┠──┬ {} DashLoader load", TimeUtil.getTimeString(EXPORT_TIME));
			LOGGER.info("┃  ├── {} File reading", TimeUtil.getTimeString(EXPORT_READING_TIME));
			LOGGER.info("┃  ├── {} Asset exporting", TimeUtil.getTimeString(EXPORT_EXPORTING_TIME));
			LOGGER.info("┃  └── {} Asset loading", TimeUtil.getTimeString(EXPORT_LOADING_TIME));
			EXPORT_TIME = -1;
		}
		if (MixinThings.FALLBACK_MODELS_COUNT != -1) {
			long totalModels = MixinThings.CACHED_MODELS_COUNT + MixinThings.FALLBACK_MODELS_COUNT;
			LOGGER.info("┠──┬ {}% Cache coverage", (int)(((MixinThings.CACHED_MODELS_COUNT / (float)totalModels) * 100)));
			LOGGER.info("┃  ├── {} Fallback models", MixinThings.FALLBACK_MODELS_COUNT);
			LOGGER.info("┃  └── {} Cached models", MixinThings.CACHED_MODELS_COUNT);
			MixinThings.CACHED_MODELS_COUNT = -1;
			MixinThings.FALLBACK_MODELS_COUNT = -1;
		}
		LOGGER.info("┠── {} Minecraft client reload", TimeUtil.getTimeStringFromStart(DashLoader.RELOAD_START));
		LOGGER.info("┠── {} Minecraft bootstrap", TimeUtil.getTimeString(MixinThings.BOOTSTRAP_END - MixinThings.BOOTSTRAP_START));
		LOGGER.info("┖── {} Total loading", TimeUtil.getTimeString(ManagementFactory.getRuntimeMXBean().getUptime()));

		if (DashLoader.isWrite()) {
			// Yes this is bad. But it makes us not require Fabric API
			var langCode = MinecraftClient.getInstance().getLanguageManager().getLanguage().getCode();
			var stream = this.getClass().getClassLoader().getResourceAsStream("assets/dashloader/lang/" + langCode + ".json");
			var map = new HashMap<String, String>();
			if (stream != null) {
				Language.load(stream, map::put);
			}
			DashLoaderCore.PROGRESS.setTranslations(map);
			client.currentScreen = new DashCachingScreen(client.currentScreen);
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
