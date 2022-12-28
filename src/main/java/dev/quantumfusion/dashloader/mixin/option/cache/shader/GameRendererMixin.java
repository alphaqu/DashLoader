package dev.quantumfusion.dashloader.mixin.option.cache.shader;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

import static dev.quantumfusion.dashloader.DashLoader.DL;

@Mixin(value = GameRenderer.class, priority = 69)
public abstract class GameRendererMixin {
	@Inject(method = "loadPrograms", at = @At(value = "HEAD"))
	private void prepareCache(ResourceFactory factory, CallbackInfo ci) {
		if (DL.isWrite()) {
			DL.getData().shaders.setMinecraftData(new Object2ObjectOpenHashMap<>());
		}
	}

	@Redirect(
			method = "loadPrograms",
			at = @At(
					value = "NEW",
					target = "(Lnet/minecraft/resource/ResourceFactory;Ljava/lang/String;Lnet/minecraft/client/render/VertexFormat;)Lnet/minecraft/client/gl/ShaderProgram;"
			)
	)
	private ShaderProgram shaderCreation(ResourceFactory factory, String name, VertexFormat format) throws IOException {
		// Checks if DashLoader is active
		if (DL.isRead()) {
			var data = DL.getData();
			// If we are reading from cache load the shader and check if its cached.
			var shader = data.shaders.getCacheResultData().get(name);
			if (shader != null) {
				// Loads OpenGL shader.
				data.getReadContextData().shaderData.get(name).apply();
				return shader;
			}
		} else if (DL.isWrite()) {
			// Create a shader and cache it.
			var shader = new ShaderProgram(factory, name, format);
			DL.getData().shaders.getMinecraftData().put(name, shader);
			return shader;
		}

		return new ShaderProgram(factory, name, format);
	}


}
