package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.client.texture.TextureStitcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureStitcher.class)
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
