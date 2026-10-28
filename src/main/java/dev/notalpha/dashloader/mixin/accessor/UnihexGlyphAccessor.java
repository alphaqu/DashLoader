package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(UnihexProvider.Glyph.class)
public interface UnihexGlyphAccessor {
	@Accessor("contents")
	UnihexProvider.LineData getContents();

	@Accessor("left")
	int getLeft();

	@Accessor("right")
	int getRight();

	@Invoker("<init>")
	static UnihexProvider.Glyph create(UnihexProvider.LineData contents, int left, int right) {
		throw new AssertionError();
	}
}
