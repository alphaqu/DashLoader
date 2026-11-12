package dev.notalpha.dashloader.mixin.accessor;

import org.lwjgl.util.freetype.FT_Face;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import java.nio.ByteBuffer;
import net.minecraft.client.gui.font.CodepointMap;

@Mixin(TrueTypeGlyphProvider.class)
public interface TrueTypeFontAccessor {
	@Accessor
	@Mutable
	void setFontMemory(ByteBuffer thing);

	@Accessor
	FT_Face getFace();

	@Accessor
	@Mutable
	void setFace(FT_Face thing);

	@Accessor
	float getOversample();

	@Accessor
	@Mutable
	void setOversample(float thing);

	@Accessor
	@Mutable
	void setGlyphs(CodepointMap<TrueTypeGlyphProvider.GlyphEntry> container);
}
