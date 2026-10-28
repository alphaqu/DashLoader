package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.collection.IntObjectList;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.mixin.accessor.BitmapFontAccessor;
import java.util.ArrayList;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.providers.BitmapProvider;

public final class DashBitmapFont implements DashObject<BitmapProvider, BitmapProvider> {
	public final int imageId;
	public final int image;
	public final IntObjectList<DashBitmapFontGlyph> glyphs;

	public DashBitmapFont(int imageId, int image,
	                      IntObjectList<DashBitmapFontGlyph> glyphs) {
		this.imageId = imageId;
		this.image = image;
		this.glyphs = glyphs;
	}

	public DashBitmapFont(BitmapProvider bitmapFont, RegistryWriter writer) {
		BitmapFontAccessor font = ((BitmapFontAccessor) bitmapFont);
		Object holder = BitmapFontReflection.getImageData(bitmapFont);
		this.imageId = writer.add(BitmapFontReflection.getIdentifier(holder));
		this.image = writer.add(BitmapFontReflection.getImage(holder));
		this.glyphs = new IntObjectList<>(new ArrayList<>());
		font.getGlyphs().forEach((integer, bitmapFontGlyph) -> this.glyphs.put(integer, new DashBitmapFontGlyph(bitmapFontGlyph)));
	}

	public BitmapProvider export(RegistryReader reader) {
		Object holder = BitmapFontReflection.newHolder(reader.get(this.imageId), reader.get(this.image));
		CodepointMap<BitmapProvider.Glyph> out = new CodepointMap<>(
				BitmapProvider.Glyph[]::new,
				BitmapProvider.Glyph[][]::new
		);
		this.glyphs.forEach((key, value) -> out.put(key, value.export(holder)));
		return BitmapFontReflection.newProvider(holder, out);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashBitmapFont that = (DashBitmapFont) o;

		if (imageId != that.imageId) return false;
		if (image != that.image) return false;
		return glyphs.equals(that.glyphs);
	}

	@Override
	public int hashCode() {
		int result = imageId;
		result = 31 * result + image;
		result = 31 * result + glyphs.hashCode();
		return result;
	}
}
