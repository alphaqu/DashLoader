package dev.notalpha.dashloader.client.shader;

import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;

public final class DashGlUniform {
	public final int count;
	public final int dataType;
	public final boolean loaded;

	public final String name;

	public DashGlUniform(int count, int dataType, boolean loaded, String name) {
		this.count = count;
		this.dataType = dataType;
		this.loaded = loaded;
		this.name = name;
	}

	public DashGlUniform(GlUniform glUniform, boolean loaded) {
		this.count = glUniform.getCount();
		this.dataType = glUniform.getDataType();
		this.name = glUniform.getName();
		this.loaded = loaded;
	}

	public GlUniform export(GlShader shader) {
		return new GlUniform(this.name, this.dataType, this.count, shader);
	}
}
