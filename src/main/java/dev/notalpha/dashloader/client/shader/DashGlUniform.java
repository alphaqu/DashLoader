package dev.notalpha.dashloader.client.shader;

import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;

public final class DashGlUniform {
	public final int count;
	public final int dataType;

	public final String name;

	public DashGlUniform(int count, int dataType, String name) {
		this.count = count;
		this.dataType = dataType;
		this.name = name;
	}

	public DashGlUniform(GlUniform glUniform) {
		this.count = glUniform.getCount();
		this.dataType = glUniform.getDataType();
		this.name = glUniform.getName();
	}

	public GlUniform export(ShaderProgram shader) {
		return new GlUniform(this.name, this.dataType, this.count, shader);
	}
}
