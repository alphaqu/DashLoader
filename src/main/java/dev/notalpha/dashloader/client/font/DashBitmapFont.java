package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.io.data.collection.IntObjectList;
import dev.notalpha.dashloader.mixin.accessor.BitmapFontAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.font.BitmapFont;

import java.util.ArrayList;

public final class DashBitmapFont implements DashObject<BitmapFont> {
	public final int image;
	public final IntObjectList<DashBitmapFontGlyph> glyphs;

	public DashBitmapFont(int image,
						  IntObjectList<DashBitmapFontGlyph> glyphs) {
		this.image = image;
		this.glyphs = glyphs;
	}

	public DashBitmapFont(BitmapFont bitmapFont, RegistryWriter writer) {
		BitmapFontAccessor font = ((BitmapFontAccessor) bitmapFont);
		this.image = writer.add(font.getImage());
		this.glyphs = new IntObjectList<>(new ArrayList<>());
		font.getGlyphs().forEach((integer, bitmapFontGlyph) -> this.glyphs.put(integer, new DashBitmapFontGlyph(bitmapFontGlyph, writer)));
	}

	public BitmapFont export(RegistryReader reader) {
		Int2ObjectOpenHashMap<BitmapFont.BitmapFontGlyph> out = new Int2ObjectOpenHashMap<>();
		this.glyphs.forEach((key, value) -> out.put(key, value.export(reader)));
		return BitmapFontAccessor.init(reader.get(this.image), out);
	}

}
