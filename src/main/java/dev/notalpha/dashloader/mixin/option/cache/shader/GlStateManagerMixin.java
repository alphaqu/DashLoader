package dev.notalpha.dashloader.mixin.option.cache.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.minecraft.shader.ShaderCacheHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {

	@Inject(
			method = "glShaderSource",
			at = @At(value = "HEAD")
	)
	private static void glShaderSourceInject(int shader, List<String> strings, CallbackInfo ci) {
		ShaderCacheHandler.WRITE_PROGRAM_SOURCES.visit(DashLoader.Status.SAVE, map -> {
			map.put(shader, strings);
		});
	}

}

