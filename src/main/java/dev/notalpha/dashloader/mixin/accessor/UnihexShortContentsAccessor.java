package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(UnihexProvider.ShortContents.class)
public interface UnihexShortContentsAccessor {
	@Accessor("contents")
	short[] getContents();

	@Invoker("<init>")
	static UnihexProvider.ShortContents create(short[] contents) {
		throw new AssertionError();
	}
}
