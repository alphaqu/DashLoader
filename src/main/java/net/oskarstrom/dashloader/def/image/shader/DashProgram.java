package net.oskarstrom.dashloader.def.image.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.gl.EffectProgram;
import net.minecraft.client.gl.Program;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.mixin.accessor.EffectProgramAccessor;
import net.oskarstrom.dashloader.def.mixin.accessor.ProgramAccessor;
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
				DashLoader.getVanillaData().getProgramData(access.getShaderRef()));
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
