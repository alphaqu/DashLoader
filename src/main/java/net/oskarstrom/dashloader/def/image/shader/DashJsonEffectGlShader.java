package net.oskarstrom.dashloader.def.image.shader;

import net.oskarstrom.dashloader.def.mixin.accessor.JsonEffectGlShaderAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.gl.JsonEffectGlShader;

import java.util.List;

public class DashJsonEffectGlShader {
	@Serialize(order = 0)
	public String name;
	@Serialize(order = 1)
	public DashGlBlendState blendState;
	@Serialize(order = 2)
	public List<String> attribNames;
	@Serialize(order = 3)
	public DashProgram vertexShader;
	@Serialize(order = 4)
	public DashProgram fragmentShader;

	public DashJsonEffectGlShader(@Deserialize("name") String name,
								  @Deserialize("blendState") DashGlBlendState blendState,
								  @Deserialize("attribNames") List<String> attribNames,
								  @Deserialize("vertexShader") DashProgram vertexShader,
								  @Deserialize("fragmentShader") DashProgram fragmentShader) {
		this.name = name;
		this.blendState = blendState;
		this.attribNames = attribNames;
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
	}

	public DashJsonEffectGlShader(JsonEffectGlShader jsonEffectGlShader) {
		JsonEffectGlShaderAccessor jsonEffectGlShaderAccess = (JsonEffectGlShaderAccessor) jsonEffectGlShader;
		name = jsonEffectGlShader.getName();
		blendState = new DashGlBlendState(jsonEffectGlShaderAccess.getBlendState());
		attribNames = jsonEffectGlShaderAccess.getAttribNames();
		vertexShader = new DashProgram(jsonEffectGlShader.getVertexShader());
		fragmentShader = new DashProgram(jsonEffectGlShader.getFragmentShader());
	}

	public void toUndash() {

	}


}
