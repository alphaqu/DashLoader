package net.oskarstrom.dashloader.def.font;

import net.oskarstrom.dashloader.core.data.PairMap;
import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.mixin.accessor.BitmapFontAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.font.BitmapFont;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.registry.Pointer;

@DashObject(BitmapFont.class)
public class DashBitmapFont implements DashFont {
	@Serialize(order = 0)
	public final int image;
	@Serialize(order = 1)
	public final PairMap<Integer, DashBitmapFontGlyph> glyphs;

	public DashBitmapFont(@Deserialize("image") int image,
						  @Deserialize("glyphs") PairMap<Integer,DashBitmapFontGlyph> glyphs) {
		this.image = image;
		this.glyphs = glyphs;
	}

	public DashBitmapFont(BitmapFont bitmapFont, DashRegistry registry) {
		BitmapFontAccessor font = ((BitmapFontAccessor) bitmapFont);
		image = registry.add(font.getImage());
		glyphs = new PairMap<>();
		font.getGlyphs().forEach((integer, bitmapFontGlyph) -> glyphs.add(new PairMap.Entry<>(integer, new DashBitmapFontGlyph(bitmapFontGlyph, registry))));
	}

	public BitmapFont toUndash(DashExportHandler exportHandler) {
		Int2ObjectOpenHashMap<BitmapFont.BitmapFontGlyph> out = new Int2ObjectOpenHashMap<>();
		glyphs.forEach((entry) -> out.put(entry.getKey(), entry.getValue().toUndash(registry)));
		return BitmapFontAccessor.init(registry.get(image), out);
	}

}
