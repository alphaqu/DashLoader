package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(UnihexProvider.class)
public interface UnihexProviderAccessor {
	@Invoker("<init>")
	static UnihexProvider create(CodepointMap<?> glyphs) {
		throw new AssertionError();
	}

	@Accessor("glyphs")
	CodepointMap<?> getGlyphs();
}
