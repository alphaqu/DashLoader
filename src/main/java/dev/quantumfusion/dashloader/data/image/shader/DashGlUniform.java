package dev.quantumfusion.dashloader.data.image.shader;

import dev.quantumfusion.dashloader.mixin.accessor.GlUniformAccessor;
import dev.quantumfusion.dashloader.util.DashUtil;
import dev.quantumfusion.dashloader.util.IOHelper;
import dev.quantumfusion.dashloader.util.UnsafeHelper;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import java.util.List;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;

public final class DashGlUniform {
	public final int count;
	public final int dataType;
	@DataNullable
	public final int[] intData;
	@DataNullable
	public final float[] floatData;
	public final String name;


	public DashGlUniform(int count, int dataType, int[] intData, float[] floatData, String name) {
		this.count = count;
		this.dataType = dataType;
		this.intData = intData;
		this.floatData = floatData;
		this.name = name;
	}

	public DashGlUniform(GlUniform glUniform) {
		this.count = glUniform.getCount();
		this.dataType = glUniform.getDataType();
		this.intData = DashUtil.nullable(glUniform.getIntData(), IOHelper::toArray);
		this.floatData = DashUtil.nullable(glUniform.getFloatData(), IOHelper::toArray);
		this.name = glUniform.getName();
	}

	public GlUniform export(GlShader shader, List<GlUniform> uniforms) {
		final GlUniform glUniform = UnsafeHelper.allocateInstance(GlUniform.class);
		GlUniformAccessor glUniformAccess = (GlUniformAccessor) glUniform;
		glUniformAccess.setCount(this.count);
		glUniformAccess.setDataType(this.dataType);
		glUniformAccess.setProgram(shader);
		glUniformAccess.setIntData(DashUtil.nullable(this.intData, IOHelper::fromArray));
		glUniformAccess.setFloatData(DashUtil.nullable(this.floatData, IOHelper::fromArray));
		glUniformAccess.setName(this.name);
		uniforms.add(glUniform);
		return glUniform;
	}
}
