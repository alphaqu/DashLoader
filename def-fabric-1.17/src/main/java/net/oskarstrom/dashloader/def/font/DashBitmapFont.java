package net.oskarstrom.dashloader.def.font;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.font.BitmapFont;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.registry.Pointer;
import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.mixin.accessor.BitmapFontAccessor;

@DashObject(BitmapFont.class)
public class DashBitmapFont implements DashFont {
	@Serialize(order = 0)
	public final Pointer image;
	@Serialize(order = 1)
	public final Int2ObjectMap<DashBitmapFontGlyph> glyphs;

	public DashBitmapFont(@Deserialize("image") Pointer image,
						  @Deserialize("glyphs") Int2ObjectMap<DashBitmapFontGlyph> glyphs) {
		this.image = image;
		this.glyphs = glyphs;
	}

	public DashBitmapFont(BitmapFont bitmapFont, DashRegistry registry) {
		BitmapFontAccessor font = ((BitmapFontAccessor) bitmapFont);
		image = registry.add(font.getImage());
		glyphs = new Int2ObjectOpenHashMap<>();
		font.getGlyphs().forEach((integer, bitmapFontGlyph) -> glyphs.put(integer, new DashBitmapFontGlyph(bitmapFontGlyph, registry)));
	}

	public BitmapFont toUndash(DashRegistry registry) {
		Int2ObjectOpenHashMap<BitmapFont.BitmapFontGlyph> out = new Int2ObjectOpenHashMap<>();
		glyphs.int2ObjectEntrySet().forEach((entry) -> out.put(entry.getIntKey(), entry.getValue().toUndash(registry)));
		return BitmapFontAccessor.init(registry.get(image), out);
	}

}
