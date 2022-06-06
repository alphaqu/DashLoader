package dev.quantumfusion.dashloader.mixin.main;

import dev.quantumfusion.dashloader.DashLoader;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainMixin {

	@Inject(
			method = "main([Ljava/lang/String;)V",
			at = @At(value = "HEAD")
	)
	private static void main(String[] args, CallbackInfo ci) {
		DashLoader.init();
	}
}
