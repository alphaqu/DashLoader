package net.oskarstrom.dashloader.def.image.shader;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import net.oskarstrom.dashloader.def.mixin.accessor.GlUniformAccessor;
import net.oskarstrom.dashloader.def.util.IOHelper;
import net.oskarstrom.dashloader.def.util.UnsafeHelper;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

@Data
public class DashGlUniform {
	public final int count;
	public final int dataType;
	public final int[] intData;
	public final float[] floatData;
	public final String name;


	public DashGlUniform(int count,
						 int dataType,
						 int[] intData,
						 float[] floatData,
						 String name) {
		this.count = count;
		this.dataType = dataType;
		this.intData = intData;
		this.floatData = floatData;
		this.name = name;
	}

	public DashGlUniform(GlUniform glUniform) {
		this.count = glUniform.getCount();
		this.dataType = glUniform.getDataType();
		final IntBuffer intData = glUniform.getIntData();
		if (intData != null) {
			this.intData = IOHelper.toArray(intData, count);
		} else {
			this.intData = new int[0];
		}

		final FloatBuffer floatData = glUniform.getFloatData();
		if (floatData != null) {
			this.floatData = IOHelper.toArray(floatData, count);
		} else {
			this.floatData = new float[0];
		}

		this.name = glUniform.getName();
	}

	public GlUniform toUndash(GlShader shader, List<GlUniform> uniforms) {
		final GlUniform glUniform = UnsafeHelper.allocateInstance(GlUniform.class);
		GlUniformAccessor glUniformAccess = (GlUniformAccessor) glUniform;
		glUniformAccess.setCount(this.count);
		glUniformAccess.setDataType(this.dataType);
		glUniformAccess.setProgram(shader);

		if (floatData.length == 0) {
			glUniformAccess.setFloatData(null);
		} else {
			final FloatBuffer floatBuffer = MemoryUtil.memAllocFloat(count);
			floatBuffer.put(floatData);
			floatBuffer.flip();
			glUniformAccess.setFloatData(floatBuffer);
		}

		if (intData.length == 0) {
			glUniformAccess.setIntData(null);
		} else {
			final IntBuffer intBuffer = MemoryUtil.memAllocInt(count);
			intBuffer.put(intData);
			intBuffer.flip();
			glUniformAccess.setIntData(intBuffer);
		}

		glUniformAccess.setName(this.name);
		uniforms.add(glUniform);
		return glUniform;
	}
}
