package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.mixin.accessor.BitmapFontGlyphAccessor;
import net.minecraft.client.gui.font.providers.BitmapProvider;

public final class DashBitmapFontGlyph {
	public final float scaleFactor;
	public final int x;
	public final int y;
	public final int width;
	public final int height;
	public final int advance;
	public final int ascent;

	public DashBitmapFontGlyph(float scaleFactor, int x, int y, int width, int height, int advance, int ascent) {
		this.scaleFactor = scaleFactor;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.advance = advance;
		this.ascent = ascent;
	}

	public DashBitmapFontGlyph(BitmapProvider.Glyph bitmapFontGlyph) {
		BitmapFontGlyphAccessor font = ((BitmapFontGlyphAccessor) (Object) bitmapFontGlyph);
		this.scaleFactor = font.getScale();
		this.x = font.getX();
		this.y = font.getY();
		this.width = font.getWidth();
		this.height = font.getHeight();
		this.advance = font.getAdvance();
		this.ascent = font.getAscent();
	}

	public BitmapProvider.Glyph export(Object imageData) {
		return BitmapFontReflection.newGlyph(this.scaleFactor, imageData, this.x, this.y, this.width, this.height, this.advance, this.ascent);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashBitmapFontGlyph that = (DashBitmapFontGlyph) o;

		if (Float.compare(that.scaleFactor, scaleFactor) != 0) return false;
		if (x != that.x) return false;
		if (y != that.y) return false;
		if (width != that.width) return false;
		if (height != that.height) return false;
		if (advance != that.advance) return false;
		return ascent == that.ascent;
	}

	@Override
	public int hashCode() {
		int result = Float.hashCode(scaleFactor);
		result = 31 * result + x;
		result = 31 * result + y;
		result = 31 * result + width;
		result = 31 * result + height;
		result = 31 * result + advance;
		result = 31 * result + ascent;
		return result;
	}
}
