package dev.notalpha.dashloader.mixin.accessor;

import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import org.lwjgl.util.freetype.FT_Face;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrueTypeGlyphProvider.class)
public interface TrueTypeGlyphProviderAccessor {
	@Accessor("face")
	FT_Face getFace();

	@Accessor("oversample")
	float getOversample();
}
