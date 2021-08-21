package net.oskarstrom.dashloader.def.image.shader;

import net.oskarstrom.dashloader.def.mixin.accessor.GlUniformAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.util.IOHelper;
import net.oskarstrom.dashloader.def.util.UnsafeHelper;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class DashGlUniform {

	@Serialize(order = 0)
	public final int count;
	@Serialize(order = 1)
	public final int dataType;
	@Serialize(order = 2)
	@SerializeNullable
	public final int[] intData;

	@Serialize(order = 3)
	@SerializeNullable
	public final float[] floatData;

	@Serialize(order = 4)
	public final String name;


	public DashGlUniform(@Deserialize("count") int count,
						 @Deserialize("dataType") int dataType,
						 @Deserialize("intData") int[] intData,
						 @Deserialize("floatData") float[] floatData,
						 @Deserialize("name") String name) {
		this.count = count;
		this.dataType = dataType;
		this.intData = intData;
		this.floatData = floatData;
		this.name = name;
	}

	public DashGlUniform(GlUniform glUniform) {
		this.count = glUniform.getCount();
		this.dataType = glUniform.getDataType();
		this.intData = DashHelper.nullable(glUniform.getIntData(), (i) -> IOHelper.toArray(i, count));
		this.floatData = DashHelper.nullable(glUniform.getFloatData(), (f) -> IOHelper.toArray(f, count));
		this.name = glUniform.getName();
	}

	public GlUniform toUndash(GlShader shader, List<GlUniform> uniforms) {
		final GlUniform glUniform = UnsafeHelper.allocateInstance(GlUniform.class);
		GlUniformAccessor glUniformAccess = (GlUniformAccessor) glUniform;
		glUniformAccess.setCount(this.count);
		glUniformAccess.setDataType(this.dataType);
		glUniformAccess.setProgram(shader);
		glUniformAccess.setFloatData(DashHelper.nullable(floatData, floatData -> {
			final FloatBuffer floatBuffer = MemoryUtil.memAllocFloat(count);
			floatBuffer.put(floatData);
			floatBuffer.flip();
			return floatBuffer;
		}));
		glUniformAccess.setIntData(DashHelper.nullable(intData, intData -> {
			final IntBuffer intBuffer = MemoryUtil.memAllocInt(count);
			intBuffer.put(intData);
			intBuffer.flip();
			return intBuffer;
		}));
		glUniformAccess.setName(this.name);
		uniforms.add(glUniform);
		return glUniform;
	}
}
