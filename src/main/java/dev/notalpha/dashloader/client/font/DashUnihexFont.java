package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.collection.IntObjectList;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.mixin.accessor.UnihexFontAccessor;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.providers.UnihexProvider;

public final class DashUnihexFont implements DashObject<UnihexProvider, UnihexProvider> {
	public final IntObjectList<UnihexProvider.Glyph> glyphs;

	public DashUnihexFont(IntObjectList<UnihexProvider.Glyph> glyphs) {
		this.glyphs = glyphs;
	}

	public DashUnihexFont(UnihexProvider rawFont, RegistryWriter writer) {
		this.glyphs = new IntObjectList<>();
		var font = ((UnihexFontAccessor) rawFont);
		var fontImages = font.getGlyphs();
		fontImages.forEach(this.glyphs::put);
	}

	public UnihexProvider export(RegistryReader handler) {
		CodepointMap<UnihexProvider.Glyph> container = new CodepointMap<>(
				UnihexProvider.Glyph[]::new,
				UnihexProvider.Glyph[][]::new
		);
		this.glyphs.forEach(container::put);
		return UnihexFontAccessor.create(container);
	}

	public static class DashUnicodeTextureGlyph {
		public final UnihexProvider.LineData contents;
		public final int left;
		public final int right;

		public DashUnicodeTextureGlyph(UnihexProvider.LineData contents, int left, int right) {
			this.contents = contents;
			this.left = left;
			this.right = right;
		}

		public DashUnicodeTextureGlyph(UnihexProvider.Glyph glyph) {
			this.contents = glyph.contents();
			this.left = glyph.left();
			this.right = glyph.right();
		}
	}
}
