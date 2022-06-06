package dev.quantumfusion.dashloader.mixin.main;

import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.client.DashCachingScreen;
import dev.quantumfusion.dashloader.util.TimeUtil;
import dev.quantumfusion.dashloader.util.mixins.MixinThings;
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

		DashLoader.LOGGER.info("┏ DashLoader Statistics.");
		if (DashLoader.EXPORT_TIME != -1) {
			DashLoader.LOGGER.info("┠──┬ {} DashLoader load", TimeUtil.getTimeString(DashLoader.EXPORT_TIME));
			DashLoader.LOGGER.info("┃  ├── {} File reading", TimeUtil.getTimeString(DashLoader.EXPORT_READING_TIME));
			DashLoader.LOGGER.info("┃  ├── {} Asset exporting", TimeUtil.getTimeString(DashLoader.EXPORT_EXPORTING_TIME));
			DashLoader.LOGGER.info("┃  └── {} Asset loading", TimeUtil.getTimeString(DashLoader.EXPORT_LOADING_TIME));
			DashLoader.EXPORT_TIME = -1;
		}
		if (MixinThings.FALLBACK_MODELS_COUNT != -1) {
			long totalModels = MixinThings.CACHED_MODELS_COUNT + MixinThings.FALLBACK_MODELS_COUNT;
			DashLoader.LOGGER.info("┠──┬ {}% Cache coverage", (int) (((MixinThings.CACHED_MODELS_COUNT / (float) totalModels) * 100)));
			DashLoader.LOGGER.info("┃  ├── {} Fallback models", MixinThings.FALLBACK_MODELS_COUNT);
			DashLoader.LOGGER.info("┃  └── {} Cached models", MixinThings.CACHED_MODELS_COUNT);
			MixinThings.CACHED_MODELS_COUNT = -1;
			MixinThings.FALLBACK_MODELS_COUNT = -1;
		}
		DashLoader.LOGGER.info("┠── {} Minecraft client reload", TimeUtil.getTimeStringFromStart(DashLoader.RELOAD_START));
		DashLoader.LOGGER.info("┠── {} Minecraft bootstrap", TimeUtil.getTimeString(MixinThings.BOOTSTRAP_END - MixinThings.BOOTSTRAP_START));
		DashLoader.LOGGER.info("┖── {} Total loading", TimeUtil.getTimeString(ManagementFactory.getRuntimeMXBean().getUptime()));

		if (DashLoader.isWrite()) {
			// Yes this is bad. But it makes us not require Fabric API
			var langCode = MinecraftClient.getInstance().getLanguageManager().getLanguage().getCode();
			DashLoader.LOGGER.info(langCode);
			var stream = this.getClass().getClassLoader().getResourceAsStream("assets/dashloader/lang/" + langCode + ".json");
			var map = new HashMap<String, String>();
			if (stream != null) {
				DashLoader.LOGGER.info("Found translations");
				Language.load(stream, map::put);
			} else {
				var en_stream = this.getClass().getClassLoader().getResourceAsStream("assets/dashloader/lang/en_us.json");
				if (en_stream != null) {
					Language.load(en_stream, map::put);
				}
			}
			DashLoader.LOGGER.info("Missing translations");
			DashLoader.INSTANCE.progress.setTranslations(map);
			this.client.currentScreen = new DashCachingScreen(this.client.currentScreen);
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
