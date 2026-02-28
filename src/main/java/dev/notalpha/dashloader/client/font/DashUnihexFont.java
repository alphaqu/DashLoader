package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.collection.IntObjectList;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.mixin.accessor.UnihexProviderAccessor;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.providers.UnihexProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class DashUnihexFont implements DashObject<UnihexProvider, UnihexProvider> {
	public final IntObjectList<DashUnicodeTextureGlyph> glyphs;

	public DashUnihexFont(IntObjectList<DashUnicodeTextureGlyph> glyphs) {
		this.glyphs = glyphs;
	}

	public DashUnihexFont(UnihexProvider rawFont, RegistryWriter writer) {
		this.glyphs = new IntObjectList<>();
		var font = ((UnihexProviderAccessor) rawFont);
		font.getGlyphs().forEach((codepoint, glyph) -> this.glyphs.put(codepoint, new DashUnicodeTextureGlyph(glyph)));
	}

	@Override
	public UnihexProvider export(RegistryReader handler) {
		CodepointMap<Object> container = new CodepointMap<>(Object[]::new, i -> new Object[i][]);
		this.glyphs.forEach((codepoint, glyph) -> container.put(codepoint, glyph.exportGlyph()));
		return UnihexProviderAccessor.create(container);
	}

	public static class DashUnicodeTextureGlyph {
		public final byte[] bytes;
		public final short[] shorts;
		public final int[] ints;
		public final int bitWidth;
		public final int left;
		public final int right;

		public DashUnicodeTextureGlyph(byte[] bytes, short[] shorts, int[] ints, int bitWidth, int left, int right) {
			this.bytes = bytes;
			this.shorts = shorts;
			this.ints = ints;
			this.bitWidth = bitWidth;
			this.left = left;
			this.right = right;
		}

		public DashUnicodeTextureGlyph(Object glyph) {
			try {
				Class<?> glyphClass = glyph.getClass();
				Method contentsMethod = glyphClass.getDeclaredMethod("contents");
				Method leftMethod = glyphClass.getDeclaredMethod("left");
				Method rightMethod = glyphClass.getDeclaredMethod("right");
				contentsMethod.setAccessible(true);
				leftMethod.setAccessible(true);
				rightMethod.setAccessible(true);

				Object contents = contentsMethod.invoke(glyph);
				Method bitWidthMethod = contents.getClass().getMethod("bitWidth");
				Method lineMethod = contents.getClass().getMethod("line", int.class);
				int width = (int) bitWidthMethod.invoke(contents);

				this.left = (int) leftMethod.invoke(glyph);
				this.right = (int) rightMethod.invoke(glyph);
				this.bitWidth = width;

				if (width == 8) {
					this.bytes = new byte[16];
					for (int i = 0; i < 16; i++) {
						int line = (int) lineMethod.invoke(contents, i);
						this.bytes[i] = (byte) (line >>> 24);
					}
					this.shorts = null;
					this.ints = null;
				} else if (width == 16) {
					this.shorts = new short[16];
					for (int i = 0; i < 16; i++) {
						int line = (int) lineMethod.invoke(contents, i);
						this.shorts[i] = (short) (line >>> 16);
					}
					this.bytes = null;
					this.ints = null;
				} else {
					this.ints = new int[16];
					for (int i = 0; i < 16; i++) {
						this.ints[i] = (int) lineMethod.invoke(contents, i);
					}
					this.bytes = null;
					this.shorts = null;
				}
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Failed to snapshot Unihex glyph", e);
			}
		}

		public Object exportGlyph() {
			try {
				Object lineData = this.exportLineData();
				Class<?> lineDataClass = Class.forName("net.minecraft.client.gui.font.providers.UnihexProvider$LineData");
				Class<?> glyphClass = Class.forName("net.minecraft.client.gui.font.providers.UnihexProvider$Glyph");
				Constructor<?> glyphCtor = glyphClass.getDeclaredConstructor(lineDataClass, int.class, int.class);
				glyphCtor.setAccessible(true);
				return glyphCtor.newInstance(lineData, this.left, this.right);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Failed to rebuild Unihex glyph", e);
			}
		}

		private Object exportLineData() throws ReflectiveOperationException {
			if (this.bitWidth == 8) {
				Class<?> byteClass = Class.forName("net.minecraft.client.gui.font.providers.UnihexProvider$ByteContents");
				Constructor<?> ctor = byteClass.getDeclaredConstructor(byte[].class);
				ctor.setAccessible(true);
				return ctor.newInstance((Object) this.bytes);
			}
			if (this.bitWidth == 16) {
				Class<?> shortClass = Class.forName("net.minecraft.client.gui.font.providers.UnihexProvider$ShortContents");
				Constructor<?> ctor = shortClass.getDeclaredConstructor(short[].class);
				ctor.setAccessible(true);
				return ctor.newInstance((Object) this.shorts);
			}
			Class<?> intClass = Class.forName("net.minecraft.client.gui.font.providers.UnihexProvider$IntContents");
			Constructor<?> ctor = intClass.getDeclaredConstructor(int[].class, int.class);
			ctor.setAccessible(true);
			return ctor.newInstance(this.ints, this.bitWidth);
		}
	}
}
