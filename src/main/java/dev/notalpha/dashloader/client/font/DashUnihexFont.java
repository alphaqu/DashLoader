package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.collection.IntObjectList;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.mixin.accessor.UnihexByteContentsAccessor;
import dev.notalpha.dashloader.mixin.accessor.UnihexFontAccessor;
import dev.notalpha.dashloader.mixin.accessor.UnihexGlyphAccessor;
import dev.notalpha.dashloader.mixin.accessor.UnihexIntContentsAccessor;
import dev.notalpha.dashloader.mixin.accessor.UnihexShortContentsAccessor;
import java.util.ArrayList;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.providers.UnihexProvider;

/**
 * Unihex fonts flattened to primitives: vanilla {@code LineData} implementations
 * (Byte/Short/IntContents) are not Hyphen-scannable on all loaders (non-public
 * canonical constructors), so only raw content is cached.
 */
public final class DashUnihexFont implements DashObject<UnihexProvider, UnihexProvider> {
	public final IntObjectList<DashUnihexFontGlyph> glyphs;

	public DashUnihexFont(IntObjectList<DashUnihexFontGlyph> glyphs) {
		this.glyphs = glyphs;
	}

	public DashUnihexFont(UnihexProvider rawFont, RegistryWriter writer) {
		this.glyphs = new IntObjectList<>(new ArrayList<>());
		var font = ((UnihexFontAccessor) rawFont);
		font.getGlyphs().forEach((codepoint, glyph) -> this.glyphs.put(codepoint, new DashUnihexFontGlyph(glyph)));
	}

	public UnihexProvider export(RegistryReader handler) {
		CodepointMap<UnihexProvider.Glyph> container = new CodepointMap<>(
				UnihexProvider.Glyph[]::new,
				UnihexProvider.Glyph[][]::new
		);
		this.glyphs.forEach((codepoint, glyph) -> container.put(codepoint, glyph.export()));
		return UnihexFontAccessor.create(container);
	}

	public static final class DashUnihexFontGlyph {
		private static final byte KIND_BYTE = 0;
		private static final byte KIND_SHORT = 1;
		private static final byte KIND_INT = 2;

		public final int left;
		public final int right;
		public final byte kind;
		public final int[] data;
		public final int bitWidth;

		public DashUnihexFontGlyph(int left, int right, byte kind, int[] data, int bitWidth) {
			this.left = left;
			this.right = right;
			this.kind = kind;
			this.data = data;
			this.bitWidth = bitWidth;
		}

		public DashUnihexFontGlyph(UnihexProvider.Glyph glyph) {
			var access = (UnihexGlyphAccessor) (Object) glyph;
			this.left = access.getLeft();
			this.right = access.getRight();
			var contents = access.getContents();
			if (contents instanceof UnihexProvider.ByteContents byteContents) {
				this.kind = KIND_BYTE;
				byte[] raw = ((UnihexByteContentsAccessor) (Object) byteContents).getContents();
				this.data = new int[raw.length];
				for (int i = 0; i < raw.length; i++) {
					this.data[i] = raw[i];
				}
				this.bitWidth = 0;
			} else if (contents instanceof UnihexProvider.ShortContents shortContents) {
				this.kind = KIND_SHORT;
				short[] raw = ((UnihexShortContentsAccessor) (Object) shortContents).getContents();
				this.data = new int[raw.length];
				for (int i = 0; i < raw.length; i++) {
					this.data[i] = raw[i];
				}
				this.bitWidth = 0;
			} else if (contents instanceof UnihexProvider.IntContents intContents) {
				this.kind = KIND_INT;
				this.data = ((UnihexIntContentsAccessor) (Object) intContents).getContents().clone();
				this.bitWidth = ((UnihexIntContentsAccessor) (Object) intContents).getBitWidth();
			} else {
				throw new IllegalArgumentException("Unknown Unihex LineData: " + contents.getClass().getName());
			}
		}

		public UnihexProvider.Glyph export() {
			UnihexProvider.LineData contents;
			switch (this.kind) {
				case KIND_BYTE -> {
					byte[] raw = new byte[this.data.length];
					for (int i = 0; i < this.data.length; i++) {
						raw[i] = (byte) this.data[i];
					}
					contents = UnihexByteContentsAccessor.create(raw);
				}
				case KIND_SHORT -> {
					short[] raw = new short[this.data.length];
					for (int i = 0; i < this.data.length; i++) {
						raw[i] = (short) this.data[i];
					}
					contents = UnihexShortContentsAccessor.create(raw);
				}
				case KIND_INT -> contents = UnihexIntContentsAccessor.create(this.data.clone(), this.bitWidth);
				default -> throw new IllegalStateException("Unknown Unihex kind: " + this.kind);
			}
			return UnihexGlyphAccessor.create(contents, this.left, this.right);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			DashUnihexFontGlyph that = (DashUnihexFontGlyph) o;

			if (left != that.left) return false;
			if (right != that.right) return false;
			if (kind != that.kind) return false;
			if (bitWidth != that.bitWidth) return false;
			if (data.length != that.data.length) return false;
			for (int i = 0; i < data.length; i++) {
				if (data[i] != that.data[i]) return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int result = left;
			result = 31 * result + right;
			result = 31 * result + kind;
			result = 31 * result + bitWidth;
			result = 31 * result + data.length;
			for (int datum : data) {
				result = 31 * result + datum;
			}
			return result;
		}
	}
}
