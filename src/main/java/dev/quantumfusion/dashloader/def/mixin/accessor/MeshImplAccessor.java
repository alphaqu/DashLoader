package dev.quantumfusion.dashloader.def.mixin.accessor;

import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MeshImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MeshImpl.class)
public interface MeshImplAccessor {

	@Invoker("<init>")
	static MeshImpl create(int[] data) {
		throw new AssertionError();
	}
}
