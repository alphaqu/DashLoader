package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(UnihexProvider.IntContents.class)
public interface UnihexIntContentsAccessor {
	@Accessor("contents")
	int[] getContents();

	@Accessor("bitWidth")
	int getBitWidth();

	@Invoker("<init>")
	static UnihexProvider.IntContents create(int[] contents, int bitWidth) {
		throw new AssertionError();
	}
}
