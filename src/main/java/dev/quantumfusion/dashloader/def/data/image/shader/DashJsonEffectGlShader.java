package dev.quantumfusion.dashloader.def.data.image.shader;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.gl.JsonEffectGlShader;
import dev.quantumfusion.dashloader.def.mixin.accessor.JsonEffectGlShaderAccessor;

import java.util.List;

@Data
public record DashJsonEffectGlShader(
		String name,
		DashGlBlendState blendState,
		List<String> attribNames,
		DashProgram vertexShader,
		DashProgram fragmentShader) {


	public DashJsonEffectGlShader(JsonEffectGlShader jsonEffectGlShader) {
		this(jsonEffectGlShader, (JsonEffectGlShaderAccessor) jsonEffectGlShader);
	}

	private DashJsonEffectGlShader(JsonEffectGlShader jsonEffectGlShader, JsonEffectGlShaderAccessor jsonEffectGlShaderAccess) {
		this(
				jsonEffectGlShader.getName(),
				new DashGlBlendState(jsonEffectGlShaderAccess.getBlendState()),
				jsonEffectGlShaderAccess.getAttribNames(),
				new DashProgram(jsonEffectGlShader.getVertexShader()),
				new DashProgram(jsonEffectGlShader.getFragmentShader())
		);
	}
}
