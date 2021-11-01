package dev.quantumfusion.dashloader.def.data.image.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.mixin.accessor.EffectProgramAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.ProgramAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.gl.EffectProgram;
import net.minecraft.client.gl.Program;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public record DashProgram(Program.Type shaderType, String name, List<String> shader) {

	public DashProgram(Program program) {
		this(program, (ProgramAccessor) program);
	}

	public DashProgram(Program program, ProgramAccessor access) {
		this(
				access.getShaderType(),
				program.getName(),
				DashLoader.getData().getWriteContextData().programData.get(access.getShaderRef()));
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

	public Program exportProgram() {
		final Program program = ProgramAccessor.create(shaderType, createProgram(shaderType), name);
		shaderType.getProgramCache().put(name, program);
		return program;
	}

	public EffectProgram exportEffectProgram() {
		return EffectProgramAccessor.create(shaderType, createProgram(shaderType), name);
	}

}
