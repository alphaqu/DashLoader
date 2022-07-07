package dev.quantumfusion.dashloader.data.font;

import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.data.common.IntObjectList;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import it.unimi.dsi.fastutil.ints.Int2FloatArrayMap;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.SpaceFont;

@DashObject(SpaceFont.class)
public final class DashSpaceFont implements DashFont{
	public final IntObjectList<Float> glyphs;

	public DashSpaceFont(SpaceFont font) {
		this.glyphs = new IntObjectList<>();
		for (Integer providedGlyph : font.getProvidedGlyphs()) {
			Glyph glyph = font.getGlyph(providedGlyph);
			this.glyphs.put(providedGlyph, glyph.getAdvance());
		}
	}

	public DashSpaceFont(IntObjectList<Float> glyphs) {
		this.glyphs = glyphs;
	}

	@Override
	public Font export(RegistryReader exportHandler) {
		Int2FloatArrayMap int2FloatArrayMap = new Int2FloatArrayMap();
		this.glyphs.forEach((key, value) -> int2FloatArrayMap.put((Integer) key, value));
		return new SpaceFont(int2FloatArrayMap);
	}
}
