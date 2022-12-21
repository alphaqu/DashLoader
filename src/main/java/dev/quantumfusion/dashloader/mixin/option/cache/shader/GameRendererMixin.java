package dev.quantumfusion.dashloader.mixin.option.cache.shader;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.quantumfusion.dashloader.DashLoader.DL;

@Mixin(value = GameRenderer.class, priority = 69)
public abstract class GameRendererMixin {
	@Inject(method = "loadPrograms", at = @At(value = "HEAD"))
	private void prepareCache(ResourceFactory factory, CallbackInfo ci) {
		if (DL.isWrite()) {
			DL.getData().shaders.setMinecraftData(new Object2ObjectOpenHashMap<>());
		}
	}

	// TODO
	// @Redirect(
	//			method = "loadPrograms",
	//			at = @At(
	//					value = "NEW",
	//					target = "Lnet/minecraft/client/gl/ShaderProgram;<init>(Lnet/minecraft/resounrce/ResourceFactory;Ljava/lang/String;Lnet/minecraft/client/render/VertexFormat;)V"
	//			)
	//	)
	//	private ShaderProgram shaderCreation(ResourceFactory factory, String name, VertexFormat format) throws IOException {
	//		// Checks if DashLoader is active
	//		if (DL.isRead()) {
	//			var data = DL.getData();
	//			// If we are reading from cache load the shader and check if its cached.
	//			var shader = data.shaders.getCacheResultData().get(name);
	//			if (shader != null) {
	//				// Loads OpenGL shader.
	//				data.getReadContextData().shaderData.get(name).apply();
	//				return shader;
	//			}
	//		} else if (DL.isWrite()) {
	//			// Create a shader and cache it.
	//			var shader = new ShaderProgram(factory, name, format);
	//			DL.getData().shaders.getMinecraftData().put(name, shader);
	//			return shader;
	//		}
	//
	//		return new ShaderProgram(factory, name, format);
	//	}


}
