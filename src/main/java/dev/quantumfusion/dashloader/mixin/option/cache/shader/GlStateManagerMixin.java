package dev.quantumfusion.dashloader.mixin.option.cache.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static dev.quantumfusion.dashloader.DashLoader.DL;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {

	@Inject(
			method = "glShaderSource",
			at = @At(value = "HEAD")
	)
	private static void glShaderSourceInject(int shader, List<String> strings, CallbackInfo ci) {
		if (DL.isWrite()) {
			DL.getData().getWriteContextData().programData.put(shader, strings);
		}
	}

}

