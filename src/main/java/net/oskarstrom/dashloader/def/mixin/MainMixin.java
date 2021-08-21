package net.oskarstrom.dashloader.def.mixin;

import net.minecraft.client.main.Main;
import net.oskarstrom.dashloader.DashLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainMixin {

	@Inject(method = "main([Ljava/lang/String;)V",
			at = @At(value = "HEAD"), cancellable = true)
	private static void main(String[] args, CallbackInfo ci) {
		DashLoader loader = new DashLoader(Thread.currentThread().getContextClassLoader());
		loader.initialize();
	}
}
