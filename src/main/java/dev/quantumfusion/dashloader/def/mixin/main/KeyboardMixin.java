package dev.quantumfusion.dashloader.def.mixin.main;

import dev.quantumfusion.dashloader.core.DashLoaderCore;
import dev.quantumfusion.dashloader.def.DashLoader;
import net.minecraft.client.Keyboard;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes f3 + t reset the cache. Also makes shift + f3 + t not reset it.
 */
@Mixin(Keyboard.class)
public class KeyboardMixin {

	private boolean shiftHeld = false;
	@Inject(
			method = "processF3",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/MinecraftClient;reloadResources()Ljava/util/concurrent/CompletableFuture;",
					shift = At.Shift.BEFORE
			)
	)
	private void f3tReloadWorld(int key, CallbackInfoReturnable<Boolean> cir) {
		if (!shiftHeld) {
			DashLoader.LOGGER.info("Clearing cache.");
			DashLoaderCore.IO.clearCache();
		}
	}

	@Inject(
			method = "onKey",
			at = @At("HEAD")
	)
	private void keyPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
		shiftHeld = action != 0 && modifiers == GLFW.GLFW_MOD_SHIFT;
	}
}
