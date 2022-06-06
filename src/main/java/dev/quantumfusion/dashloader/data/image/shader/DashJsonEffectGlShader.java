package dev.quantumfusion.dashloader.data.image.shader;

import dev.quantumfusion.dashloader.mixin.accessor.JsonEffectGlShaderAccessor;
import dev.quantumfusion.dashloader.util.MissingDataException;
import net.minecraft.client.gl.JsonEffectGlShader;

import java.util.List;

public record DashJsonEffectGlShader(
		String name,
		DashGlBlendState blendState,
		List<String> attribNames,
		DashProgram vertexShader,
		DashProgram fragmentShader) {


	public DashJsonEffectGlShader(JsonEffectGlShader jsonEffectGlShader) throws MissingDataException {
		this(jsonEffectGlShader, (JsonEffectGlShaderAccessor) jsonEffectGlShader);
	}

	private DashJsonEffectGlShader(JsonEffectGlShader jsonEffectGlShader, JsonEffectGlShaderAccessor jsonEffectGlShaderAccess) throws MissingDataException {
		this(
				jsonEffectGlShader.getName(),
				new DashGlBlendState(jsonEffectGlShaderAccess.getBlendState()),
				jsonEffectGlShaderAccess.getAttribNames(),
				new DashProgram(jsonEffectGlShader.getVertexShader()),
				new DashProgram(jsonEffectGlShader.getFragmentShader())
		);
	}
}
