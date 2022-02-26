package dev.quantumfusion.dashloader.def.mixin.option.cache.shader;

import dev.quantumfusion.dashloader.def.DashLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(value = GameRenderer.class, priority = 69)
public abstract class GameRendererMixin {
	@Inject(method = "loadShaders(Lnet/minecraft/resource/ResourceManager;)V", at = @At(value = "HEAD"))
	private void prepareCache(ResourceManager manager, CallbackInfo ci) {
		if (DashLoader.isWrite()) {
			DashLoader.getData().shaders.setMinecraftData(new Object2ObjectOpenHashMap<>());
		}
	}

	@Redirect(
			method = "loadShaders(Lnet/minecraft/resource/ResourceManager;)V",
			at = @At(
					value = "NEW",
					target = "Lnet/minecraft/client/render/Shader;<init>"
			)
	)
	private Shader shaderCreation(ResourceFactory factory, String name, VertexFormat format) throws IOException {
		// Checks if DashLoader is active
		if (DashLoader.dataManagerActive()) {
			var data = DashLoader.getData();
			var shaders = data.shaders;
			if (DashLoader.isRead()) {
				// If we are reading from cache load the shader and check if its cached.
				var shader = shaders.getCacheResultData().get(name);
				if (shader != null) {
					// Loads OpenGL shader.
					data.getReadContextData().shaderData.get(name).apply();
					return shader;
				}
			} else if (DashLoader.isWrite()) {
				// Create a shader and cache it.
				var shader = new Shader(factory, name, format);
				shaders.getMinecraftData().put(name, shader);
				return shader;
			}
		}

		return new Shader(factory, name, format);
	}


}
