package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(UnihexProvider.ByteContents.class)
public interface UnihexByteContentsAccessor {
	@Accessor("contents")
	byte[] getContents();

	@Invoker("<init>")
	static UnihexProvider.ByteContents create(byte[] contents) {
		throw new AssertionError();
	}
}
