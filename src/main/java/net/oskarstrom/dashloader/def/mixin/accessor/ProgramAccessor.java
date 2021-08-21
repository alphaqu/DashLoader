package net.oskarstrom.dashloader.def.mixin.accessor;

import net.minecraft.client.gl.Program;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Program.class)
public interface ProgramAccessor {
	@Invoker("<init>")
	static Program create(Program.Type shaderType, int shaderRef, String name) {
		throw new AssertionError();
	}

	@Accessor
	Program.Type getShaderType();

	@Accessor
	int getShaderRef();

	@Mixin(Program.Type.class)
	interface TypeAccessor {
		@Accessor
		int getGlType();
	}
}

