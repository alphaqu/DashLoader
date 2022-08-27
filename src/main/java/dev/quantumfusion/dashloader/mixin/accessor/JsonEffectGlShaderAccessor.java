package dev.quantumfusion.dashloader.mixin.accessor;

import java.util.List;
import net.minecraft.client.gl.GlBlendState;
import net.minecraft.client.gl.JsonEffectGlShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(JsonEffectGlShader.class)
public interface JsonEffectGlShaderAccessor {
	@Accessor
	GlBlendState getBlendState();

	@Accessor
	List<String> getAttribNames();

}
