package net.oskarstrom.dashloader.def.font;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.font.BitmapFont;
import net.oskarstrom.dashloader.core.data.IntObjectList;
import net.oskarstrom.dashloader.core.data.ObjectObjectList;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.mixin.accessor.BitmapFontAccessor;

import java.util.ArrayList;

@Data
@DashObject(BitmapFont.class)
public class DashBitmapFont implements DashFont {
	public final int image;
	public final IntObjectList<DashBitmapFontGlyph> glyphs;

	public DashBitmapFont(int image,
			IntObjectList<DashBitmapFontGlyph> glyphs) {
		this.image = image;
		this.glyphs = glyphs;
	}

	public DashBitmapFont(BitmapFont bitmapFont, DashRegistry registry) {
		BitmapFontAccessor font = ((BitmapFontAccessor) bitmapFont);
		image = registry.add(font.getImage());
		glyphs = new IntObjectList<>(new ArrayList<>());
		font.getGlyphs().forEach((integer, bitmapFontGlyph) -> glyphs.put(integer, new DashBitmapFontGlyph(bitmapFontGlyph, registry)));
	}

	public BitmapFont toUndash(DashExportHandler handler) {
		Int2ObjectOpenHashMap<BitmapFont.BitmapFontGlyph> out = new Int2ObjectOpenHashMap<>();
		glyphs.forEach((key, value) -> out.put(key, value.toUndash(handler)));
		return BitmapFontAccessor.init(handler.get(image), out);
	}

}
