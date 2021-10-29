package dev.quantumfusion.dashloader.def.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import dev.quantumfusion.dashloader.def.DashLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(SplashOverlay.class)
public class SplashScreenMixin {


	private static boolean printed = false;
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J", shift = At.Shift.BEFORE, ordinal = 1), cancellable = true)
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (DashLoader.getInstance().getStatus() == DashLoader.Status.LOADED) {
			this.client.setOverlay(null);
			if (client.currentScreen != null) {
				if (client.currentScreen instanceof TitleScreen) {
					client.currentScreen = new TitleScreen(false);
				}
				this.client.currentScreen.init(this.client, this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight());
			}
		} else {
			this.client.setOverlay(null);
			DashLoader.getInstance().saveDashCache();
		}
		if (!printed) {
			printed = true;
		}
		ci.cancel();
	}
}
