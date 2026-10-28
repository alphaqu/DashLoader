package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.client.renderer.texture.Stitcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Stitcher.class)
public interface StitcherAccessor {
	@Accessor
	int getMaxWidth();

	@Accessor
	int getMaxHeight();

	@Accessor
	int getMipLevel();

	@Accessor
	int getPadding();
}
