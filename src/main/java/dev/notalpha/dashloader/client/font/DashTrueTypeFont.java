package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.io.IOHelper;
import dev.notalpha.dashloader.misc.UnsafeHelper;
import dev.notalpha.dashloader.mixin.accessor.TrueTypeFontAccessor;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FreeTypeUtil;
import net.minecraft.client.font.GlyphContainer;
import net.minecraft.client.font.TrueTypeFont;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

public final class DashTrueTypeFont implements DashObject<TrueTypeFont, TrueTypeFont> {
	public final byte[] fontData;
	public final float oversample;
	public final int[] excludedCharacters;
	public final int size;
	public final float shiftX;
	public final float shiftY;
	public final int[] glyphMap;
	private transient TrueTypeFont _font;

	public DashTrueTypeFont(byte[] fontData, float oversample, int[] excludedCharacters, int size, float shiftX, float shiftY, int[] glyphMap) {
		this.fontData = fontData;
		this.oversample = oversample;
		this.excludedCharacters = excludedCharacters;
		this.size = size;
		this.shiftX = shiftX;
		this.shiftY = shiftY;
		this.glyphMap = glyphMap;
	}

	public DashTrueTypeFont(TrueTypeFont font) {
		TrueTypeFontAccessor fontAccess = (TrueTypeFontAccessor) font;
		FT_Face ft_face = fontAccess.getFace();
		FontPrams prams = FontModule.FONT_TO_DATA.get(CacheStatus.SAVE).get(ft_face);
		final Identifier ttFont = prams.id();
		byte[] data = null;
		try {
			Optional<Resource> resource = MinecraftClient.getInstance().getResourceManager().getResource(ttFont.withPrefixedPath("font/"));
			if (resource.isPresent()) {
				var stream = resource.get().getInputStream();
				data = IOHelper.streamToArray(stream);
				stream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			FT_Vector vec = FT_Vector.malloc(memoryStack);
			FreeType.FT_Get_Transform(ft_face, null, vec);
			this.shiftX = vec.x() / 64F;
			this.shiftY = vec.y() / 64F;
		}

		this.fontData = data;
		this.oversample = fontAccess.getOversample();
		this.excludedCharacters = prams.skip().codePoints().toArray();
		this.size = Math.round(prams.size() * this.oversample);
		this.glyphMap = readGlyphMap(data, this.excludedCharacters, this.size, this.shiftX, this.shiftY);
	}

	private static int[] readGlyphMap(byte[] fontData, int[] excludedCharacters, int size, float shiftX,
			float shiftY) {
		var set = new IntArraySet(excludedCharacters);
		int[] out = new int[512];
		int count = 0;

		ByteBuffer buffer = MemoryUtil.memAlloc(fontData.length);
		try {
			buffer.put(fontData);
			buffer.flip();

			synchronized (FreeTypeUtil.LOCK) {
				try (MemoryStack memoryStack = MemoryStack.stackPush()) {
					PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
					FreeTypeUtil.checkFatalError(
							FreeType.FT_New_Memory_Face(FreeTypeUtil.initialize(), buffer, 0L, pointerBuffer),
							"Initializing font face");
					FT_Face ft_face = FT_Face.create(pointerBuffer.get());
					try {
						FreeType.FT_Set_Pixel_Sizes(ft_face, size, size);
						FT_Vector vec = FreeTypeUtil.set(FT_Vector.malloc(memoryStack), shiftX, shiftY);
						FreeType.FT_Set_Transform(ft_face, null, vec);

						IntBuffer intBuffer = memoryStack.mallocInt(1);
						int j = (int) FreeType.FT_Get_First_Char(ft_face, intBuffer);

						while (true) {
							int k = intBuffer.get(0);
							if (k == 0) {
								break;
							}

							if (!set.contains(j)) {
								if (count == out.length) {
									out = java.util.Arrays.copyOf(out, out.length * 2);
								}
								out[count++] = j;
								out[count++] = k;
							}

							j = (int) FreeType.FT_Get_Next_Char(ft_face, j, intBuffer);
						}
					} finally {
						FreeType.FT_Done_Face(ft_face);
					}
				}
			}
		} finally {
			MemoryUtil.memFree(buffer);
		}

		return java.util.Arrays.copyOf(out, count);
	}

	@Override
	public TrueTypeFont export(RegistryReader handler) {
		this._font = UnsafeHelper.allocateInstance(TrueTypeFont.class);

		TrueTypeFontAccessor trueTypeFontAccess = (TrueTypeFontAccessor) this._font;
		trueTypeFontAccess.setOversample(this.oversample);
		return this._font;
	}

	@Override
	public void postExport(RegistryReader reader) {
		ByteBuffer fontBuffer = MemoryUtil.memAlloc(this.fontData.length);
		fontBuffer.put(this.fontData);
		fontBuffer.flip();

		FT_Face ft_face = null;

		var trueTypeFontAccess = (TrueTypeFontAccessor) this._font;

		var container = new GlyphContainer<>(TrueTypeFont.LazyGlyph[]::new, TrueTypeFont.LazyGlyph[][]::new);

		try {
			synchronized (FreeTypeUtil.LOCK) {
				try (MemoryStack memoryStack = MemoryStack.stackPush()) {
					PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
					FreeTypeUtil.checkFatalError(FreeType.FT_New_Memory_Face(FreeTypeUtil.initialize(), fontBuffer, 0L, pointerBuffer), "Initializing font face");
					ft_face = FT_Face.create(pointerBuffer.get());
				}
			}

			for (int i = 0; i + 1 < this.glyphMap.length; i += 2) {
				container.put(this.glyphMap[i], new TrueTypeFont.LazyGlyph(this.glyphMap[i + 1]));
			}

			trueTypeFontAccess.setContainer(container);
			trueTypeFontAccess.setFace(ft_face);
			trueTypeFontAccess.setBuffer(fontBuffer);
		} catch (Throwable e) {
			synchronized (FreeTypeUtil.LOCK) {
				if (ft_face != null) {
					FreeType.FT_Done_Face(ft_face);
				}
			}
			MemoryUtil.memFree(fontBuffer);

			throw e;
		}
	}

	public record FontPrams(Identifier id, float size, String skip) {
	}
}