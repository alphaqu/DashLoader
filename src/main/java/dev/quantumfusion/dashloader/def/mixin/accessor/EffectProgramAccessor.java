package dev.quantumfusion.dashloader.def.mixin.accessor;

import net.minecraft.client.gl.EffectProgram;
import net.minecraft.client.gl.Program;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EffectProgram.class)
public interface EffectProgramAccessor {
	@Invoker("<init>")
	static EffectProgram create(Program.Type shaderType, int shaderRef, String name) {
		throw new AssertionError();
	}
}
