package dev.quantumfusion.dashloader.mixin.accessor;

import net.minecraft.client.texture.MipmapHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MipmapHelper.class)
public interface MipmapHelperAccessor {
	@Invoker("blend")
	static int blend(int one, int two, int three, int four, boolean checkAlpha) {
		throw new AssertionError();
	}
}
