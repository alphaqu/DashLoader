package net.oskarstrom.dashloader.def.image.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import net.oskarstrom.dashloader.def.mixin.accessor.EffectProgramAccessor;
import net.oskarstrom.dashloader.def.mixin.accessor.ProgramAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.gl.EffectProgram;
import net.minecraft.client.gl.Program;
import net.oskarstrom.dashloader.def.DashLoader;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class DashProgram {
	@Serialize(order = 0)
	public final Program.Type shaderType;
	@Serialize(order = 1)
	public final String name;
	@Serialize(order = 2)
	public final List<String> shader;

	public DashProgram(@Deserialize("shaderType") Program.Type shaderType,
					   @Deserialize("name") String name,
					   @Deserialize("shader") List<String> shader) {
		this.shaderType = shaderType;
		this.name = name;
		this.shader = shader;
	}


	public DashProgram(Program program) {
		final ProgramAccessor access = (ProgramAccessor) program;
		shaderType = access.getShaderType();
		name = program.getName();
		shader = DashLoader.getVanillaData().getProgramData(access.getShaderRef());
	}

	public int createProgram(Program.Type type) {
		//noinspection ConstantConditions (MixinAccessor shit)
		int i = GlStateManager.glCreateShader(((ProgramAccessor.TypeAccessor) (Object) type).getGlType());
		GlStateManager.glShaderSource(i, shader);
		GlStateManager.glCompileShader(i);
		if (GlStateManager.glGetShaderi(i, 35713) == 0) {
			String string2 = StringUtils.trim(GlStateManager.glGetShaderInfoLog(i, 32768));
			throw new RuntimeException("Couldn't compile " + type.getName() + " : " + string2);
		} else {
			return i;
		}
	}

	public void apply() {

	}

	public Program toUndashProgram() {
		final Program program = ProgramAccessor.create(shaderType, createProgram(shaderType), name);
		shaderType.getProgramCache().put(name, program);
		return program;
	}

	public EffectProgram toUndashEffectProgram() {
		return EffectProgramAccessor.create(shaderType, createProgram(shaderType), name);
	}

}
