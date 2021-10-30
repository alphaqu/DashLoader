package dev.quantumfusion.dashloader.def.data.font;

import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.mixin.accessor.BitmapFontGlyphAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.font.BitmapFont;

@Data
public class DashBitmapFontGlyph {
	public final float scaleFactor;
	public final int image;
	public final int x;
	public final int y;
	public final int width;
	public final int height;
	public final int advance;
	public final int ascent;

	public DashBitmapFontGlyph(float scaleFactor, int image, int x, int y, int width, int height, int advance, int ascent) {
		this.scaleFactor = scaleFactor;
		this.image = image;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.advance = advance;
		this.ascent = ascent;
	}

	public DashBitmapFontGlyph(BitmapFont.BitmapFontGlyph bitmapFontGlyph, DashRegistryWriter writer) {
		BitmapFontGlyphAccessor font = ((BitmapFontGlyphAccessor) (Object) bitmapFontGlyph);
		scaleFactor = font.getScaleFactor();
		image = writer.add(font.getImage());
		x = font.getX();
		y = font.getY();
		width = font.getWidth();
		height = font.getHeight();
		advance = font.getAdvance();
		ascent = font.getAscent();
	}

	public BitmapFont.BitmapFontGlyph export(DashRegistryReader handler) {
		return BitmapFontGlyphAccessor.init(scaleFactor, handler.get(image), x, y, width, height, advance, ascent);
	}
}
