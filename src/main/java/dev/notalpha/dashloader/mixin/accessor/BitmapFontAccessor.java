package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BitmapProvider.class)
public interface BitmapFontAccessor {
	@Accessor
	CodepointMap<BitmapProvider.Glyph> getGlyphs();
}
