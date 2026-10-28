package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Identifier.class)
public interface IdentifierAccessor {
	@Invoker("<init>")
	static Identifier init(String namespace, String path) {
		throw new AssertionError();
	}
}
