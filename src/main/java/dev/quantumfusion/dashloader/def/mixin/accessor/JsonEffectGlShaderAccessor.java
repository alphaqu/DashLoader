package dev.quantumfusion.dashloader.def.mixin.accessor;

import net.minecraft.client.gl.GlBlendState;
import net.minecraft.client.gl.JsonEffectGlShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(JsonEffectGlShader.class)
public interface JsonEffectGlShaderAccessor {
	@Accessor
	GlBlendState getBlendState();

	@Accessor
	List<String> getAttribNames();

}
