package dev.notalpha.dashloader.client.font;

import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.io.IOHelper;
import dev.notalpha.dashloader.mixin.accessor.TrueTypeGlyphProviderAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

public final class DashTrueTypeFont implements DashObject<TrueTypeGlyphProvider, TrueTypeGlyphProvider> {
	public final byte[] fontData;
	public final float size;
	public final float oversample;
	public final String skip;
	public final float shiftX;
	public final float shiftY;

	public DashTrueTypeFont(byte[] fontData, float size, float oversample, String skip, float shiftX, float shiftY) {
		this.fontData = fontData;
		this.size = size;
		this.oversample = oversample;
		this.skip = skip;
		this.shiftX = shiftX;
		this.shiftY = shiftY;
	}

	public DashTrueTypeFont(TrueTypeGlyphProvider font) {
		TrueTypeGlyphProviderAccessor fontAccess = (TrueTypeGlyphProviderAccessor) font;
		FT_Face ftFace = fontAccess.getFace();
		FontPrams params = FontModule.FONT_TO_DATA.get(CacheStatus.SAVE).get(ftFace);
		if (params == null) {
			throw new IllegalStateException("Missing cached font parameters for TrueType provider");
		}

		byte[] data = null;
		try {
			Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(params.id().withPrefix("font/"));
			if (resource.isPresent()) {
				try (var stream = resource.get().open()) {
					data = IOHelper.streamToArray(stream);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to read TrueType font resource", e);
		}
		if (data == null) {
			throw new IllegalStateException("TrueType font resource was not found");
		}

		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			FT_Vector vec = FT_Vector.malloc(memoryStack);
			FreeType.FT_Get_Transform(ftFace, null, vec);
			this.shiftX = vec.x() / 64F;
			this.shiftY = vec.y() / 64F;
		}

		this.fontData = data;
		this.size = params.size();
		this.skip = params.skip();
		this.oversample = fontAccess.getOversample();
	}

	@Override
	public TrueTypeGlyphProvider export(RegistryReader handler) {
		ByteBuffer fontBuffer = MemoryUtil.memAlloc(this.fontData.length);
		fontBuffer.put(this.fontData);
		fontBuffer.flip();

		FT_Face ftFace = null;
		try {
			synchronized (FreeTypeUtil.LIBRARY_LOCK) {
				try (MemoryStack memoryStack = MemoryStack.stackPush()) {
					PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
					FreeTypeUtil.assertError(FreeType.FT_New_Memory_Face(FreeTypeUtil.getLibrary(), fontBuffer, 0L, pointerBuffer), "Initializing font face");
					ftFace = FT_Face.create(pointerBuffer.get());
				}

				String format = FreeType.FT_Get_Font_Format(ftFace);
				if (!"TrueType".equals(format)) {
					throw new IllegalStateException("Font is not in TTF format, was " + format);
				}
				FreeTypeUtil.assertError(FreeType.FT_Select_Charmap(ftFace, FreeType.FT_ENCODING_UNICODE), "Find unicode charmap");
				return new TrueTypeGlyphProvider(fontBuffer, ftFace, this.size, this.oversample, this.shiftX, this.shiftY, this.skip);
			}
		} catch (Throwable e) {
			synchronized (FreeTypeUtil.LIBRARY_LOCK) {
				if (ftFace != null) {
					FreeType.FT_Done_Face(ftFace);
				}
			}
			MemoryUtil.memFree(fontBuffer);
			throw e;
		}
	}

	public record FontPrams(ResourceLocation id, float size, String skip) {
	}
}
