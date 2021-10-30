package dev.quantumfusion.dashloader.def.data.font;

import dev.quantumfusion.dashloader.core.api.annotation.DashDependencies;
import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.common.IntObjectList;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.data.image.DashImage;
import dev.quantumfusion.dashloader.def.mixin.accessor.BitmapFontAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.font.BitmapFont;

import java.util.ArrayList;

@Data
@DashObject(BitmapFont.class)
@DashDependencies(DashImage.class)
public class DashBitmapFont implements DashFont {
	public final int image;
	public final IntObjectList<DashBitmapFontGlyph> glyphs;

	public DashBitmapFont(int image,
			IntObjectList<DashBitmapFontGlyph> glyphs) {
		this.image = image;
		this.glyphs = glyphs;
	}

	public DashBitmapFont(BitmapFont bitmapFont, DashRegistryWriter writer) {
		BitmapFontAccessor font = ((BitmapFontAccessor) bitmapFont);
		image = writer.add(font.getImage());
		glyphs = new IntObjectList<>(new ArrayList<>());
		font.getGlyphs().forEach((integer, bitmapFontGlyph) -> glyphs.put(integer, new DashBitmapFontGlyph(bitmapFontGlyph, writer)));
	}

	public BitmapFont export(DashRegistryReader reader) {
		Int2ObjectOpenHashMap<BitmapFont.BitmapFontGlyph> out = new Int2ObjectOpenHashMap<>();
		glyphs.forEach((key, value) -> out.put(key, value.export(reader)));
		return BitmapFontAccessor.init(reader.get(image), out);
	}

}
