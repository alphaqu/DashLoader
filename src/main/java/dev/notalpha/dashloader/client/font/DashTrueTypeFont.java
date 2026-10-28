package dev.notalpha.dashloader.client.font;

import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.io.IOHelper;
import dev.notalpha.dashloader.misc.UnsafeHelper;
import dev.notalpha.dashloader.mixin.accessor.TrueTypeFontAccessor;
import it.unimi.dsi.fastutil.ints.IntArraySet;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public final class DashTrueTypeFont implements DashObject<TrueTypeGlyphProvider, TrueTypeGlyphProvider> {
	public final byte[] fontData;
	public final float oversample;
	public final int[] excludedCharacters;
	public final int size;
	public final float shiftX;
	public final float shiftY;
	public final int[] glyphMap;
	private transient TrueTypeGlyphProvider _font;

	public DashTrueTypeFont(byte[] fontData, float oversample, int[] excludedCharacters, int size, float shiftX,
			float shiftY, int[] glyphMap) {
		this.fontData = fontData;
		this.oversample = oversample;
		this.excludedCharacters = excludedCharacters;
		this.size = size;
		this.shiftX = shiftX;
		this.shiftY = shiftY;
		this.glyphMap = glyphMap;
	}

	public DashTrueTypeFont(TrueTypeGlyphProvider font) {
		TrueTypeFontAccessor fontAccess = (TrueTypeFontAccessor) font;
		FT_Face ft_face = fontAccess.getFace();
		FontPrams prams = FontModule.FONT_TO_DATA.get(CacheStatus.SAVE).get(ft_face);
		final Identifier ttFont = prams.id();
		byte[] data = null;
		try {
			Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(ttFont.withPrefix("font/"));
			if (resource.isPresent()) {
				var stream = resource.get().open();
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

			synchronized (FreeTypeUtil.LIBRARY_LOCK) {
				try (MemoryStack memoryStack = MemoryStack.stackPush()) {
					PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
					FreeTypeUtil.assertError(
							FreeType.FT_New_Memory_Face(FreeTypeUtil.getLibrary(), buffer, 0L, pointerBuffer),
							"Initializing font face");
					FT_Face ft_face = FT_Face.create(pointerBuffer.get());
					try {
						FreeType.FT_Set_Pixel_Sizes(ft_face, size, size);
						FT_Vector vec = FreeTypeUtil.setVector(FT_Vector.malloc(memoryStack), shiftX, shiftY);
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
	public TrueTypeGlyphProvider export(RegistryReader handler) {
		this._font = UnsafeHelper.allocateInstance(TrueTypeGlyphProvider.class);

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

		var container = new CodepointMap<>(TrueTypeGlyphProvider.GlyphEntry[]::new, TrueTypeGlyphProvider.GlyphEntry[][]::new);

		try {
			synchronized (FreeTypeUtil.LIBRARY_LOCK) {
				try (MemoryStack memoryStack = MemoryStack.stackPush()) {
					PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
					FreeTypeUtil.assertError(FreeType.FT_New_Memory_Face(FreeTypeUtil.getLibrary(), fontBuffer, 0L, pointerBuffer), "Initializing font face");
					ft_face = FT_Face.create(pointerBuffer.get());
				}
			}

			for (int i = 0; i + 1 < this.glyphMap.length; i += 2) {
				container.put(this.glyphMap[i], new TrueTypeGlyphProvider.GlyphEntry(this.glyphMap[i + 1]));
			}

			trueTypeFontAccess.setGlyphs(container);
			trueTypeFontAccess.setFace(ft_face);
			trueTypeFontAccess.setFontMemory(fontBuffer);
		} catch (Throwable e) {
			synchronized (FreeTypeUtil.LIBRARY_LOCK) {
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