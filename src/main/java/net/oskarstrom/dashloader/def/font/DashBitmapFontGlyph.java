package net.oskarstrom.dashloader.def.font;

import net.oskarstrom.dashloader.def.mixin.accessor.BitmapFontGlyphAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.font.BitmapFont;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.registry.Pointer;

public class DashBitmapFontGlyph {
	@Serialize(order = 0)
	public final float scaleFactor;
	@Serialize(order = 1)
	public final int image;
	@Serialize(order = 2)
	public final int x;
	@Serialize(order = 3)
	public final int y;
	@Serialize(order = 4)
	public final int width;
	@Serialize(order = 5)
	public final int height;
	@Serialize(order = 6)
	public final int advance;
	@Serialize(order = 7)
	public final int ascent;

	public DashBitmapFontGlyph(@Deserialize("scaleFactor") float scaleFactor,
							   @Deserialize("image") int image,
							   @Deserialize("x") int x,
							   @Deserialize("y") int y,
							   @Deserialize("width") int width,
							   @Deserialize("height") int height,
							   @Deserialize("advance") int advance,
							   @Deserialize("ascent") int ascent
	) {
		this.scaleFactor = scaleFactor;
		this.image = image;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.advance = advance;
		this.ascent = ascent;
	}

	public DashBitmapFontGlyph(BitmapFont.BitmapFontGlyph bitmapFontGlyph, DashRegistry registry) {
		BitmapFontGlyphAccessor font = ((BitmapFontGlyphAccessor) (Object) bitmapFontGlyph);
		scaleFactor = font.getScaleFactor();
		image = registry.add(font.getImage());
		x = font.getX();
		y = font.getY();
		width = font.getWidth();
		height = font.getHeight();
		advance = font.getAdvance();
		ascent = font.getAscent();
	}

	public BitmapFont.BitmapFontGlyph toUndash(DashExportHandler exportHandler) {
		return BitmapFontGlyphAccessor.init(scaleFactor, registry.get(image), x, y, width, height, advance, ascent);
	}
}
