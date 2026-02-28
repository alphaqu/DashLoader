package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ResourceLocation.class)
public interface IdentifierAccessor {
	@Invoker("<init>")
	static ResourceLocation init(String namespace, String path) {
		throw new AssertionError();
	}
}
