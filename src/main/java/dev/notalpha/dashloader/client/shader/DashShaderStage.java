package dev.notalpha.dashloader.client.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.notalpha.dashloader.Cache;
import dev.notalpha.dashloader.mixin.accessor.ShaderStageAccessor;
import net.minecraft.client.gl.Program;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public final class DashShaderStage {
	public final Program.Type shaderType;
	public final String name;
	public final List<String> shader;

	public DashShaderStage(Program.Type shaderType, String name, List<String> shader) {
		this.shaderType = shaderType;
		this.name = name;
		this.shader = shader;
	}

	public DashShaderStage(Program program) {
		ShaderStageAccessor access = (ShaderStageAccessor) program;
		this.shaderType = access.getType();
		this.name = program.getName();
		List<String> shader = ShaderModule.WRITE_PROGRAM_SOURCES.get(Cache.Status.SAVE).get(access.getGlRef());
		if (shader == null) {
			throw new RuntimeException();
		}
		this.shader = shader;
	}

	public int createProgram(Program.Type type) {
		//noinspection ConstantConditions (MixinAccessor shit)
		int i = GlStateManager.glCreateShader(((ShaderStageAccessor.TypeAccessor) (Object) type).getGlType());
		GlStateManager.glShaderSource(i, this.shader);
		GlStateManager.glCompileShader(i);
		if (GlStateManager.glGetShaderi(i, 35713) == 0) {
			String string2 = StringUtils.trim(GlStateManager.glGetShaderInfoLog(i, 32768));
			throw new RuntimeException("Couldn't compile " + type.getName() + " : " + string2);
		} else {
			return i;
		}
	}

	public Program exportProgram() {
		Map<String, Program> loadedShaders = this.shaderType.getProgramCache();
		Program shaderStage = loadedShaders.get(this.name);
		if (shaderStage == null) {
			final Program program = ShaderStageAccessor.create(this.shaderType, this.createProgram(this.shaderType), this.name);
			loadedShaders.put(this.name, program);
			shaderStage = program;
		}
		return shaderStage;
	}
}
