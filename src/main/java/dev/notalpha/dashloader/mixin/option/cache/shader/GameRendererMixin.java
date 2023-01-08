package dev.notalpha.dashloader.mixin.option.cache.shader;

import dev.notalpha.dashloader.Cache;
import dev.notalpha.dashloader.client.shader.ShaderModule;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.util.HashMap;

@Mixin(value = GameRenderer.class, priority = 69)
public abstract class GameRendererMixin {
	@Redirect(
			method = "loadShaders(Lnet/minecraft/resource/ResourceManager;)V",
			at = @At(
					value = "NEW",
					target = "Lnet/minecraft/client/render/Shader;<init>"
			)
	)
	private Shader shaderCreation(ResourceFactory factory, String name, VertexFormat format) throws IOException {
		HashMap<String, Shader> shaders = ShaderModule.SHADERS.get(Cache.Status.LOAD);
		if (shaders != null) {
			// If we are reading from cache load the shader and check if its cached.
			var shader = shaders.get(name);
			if (shader != null) {
				// Loads OpenGL shader.
				return shader;
			}
		}

		Shader shader = new Shader(factory, name, format);
		ShaderModule.SHADERS.visit(Cache.Status.SAVE, map -> map.put(name, shader));
		return shader;
	}


}
