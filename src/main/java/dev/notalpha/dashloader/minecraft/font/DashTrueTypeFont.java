package dev.notalpha.dashloader.minecraft.font;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.io.IOHelper;
import dev.notalpha.dashloader.mixin.accessor.TrueTypeFontAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.util.UnsafeHelper;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TrueTypeFont;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@DashObject(TrueTypeFont.class)
public final class DashTrueTypeFont implements DashFont {
	public final byte[] ttfBuffer;
	public final float oversample;
	public final List<Integer> excludedCharacters;
	public final float shiftX;
	public final float shiftY;
	public final float scaleFactor;
	public final float ascent;


	public DashTrueTypeFont(byte[] ttfBuffer, float oversample, List<Integer> excludedCharacters, float shiftX, float shiftY, float scaleFactor, float ascent) {
		this.ttfBuffer = ttfBuffer;
		this.oversample = oversample;
		this.excludedCharacters = excludedCharacters;
		this.shiftX = shiftX;
		this.shiftY = shiftY;
		this.scaleFactor = scaleFactor;
		this.ascent = ascent;
	}

	public DashTrueTypeFont(TrueTypeFont font) {
		TrueTypeFontAccessor fontAccess = (TrueTypeFontAccessor) font;
		final Identifier ttFont = FontCacheHandler.FONT_TO_IDENT.get(DashLoader.Status.SAVE).get(fontAccess.getInfo());
		byte[] data = null;
		try {
			Optional<Resource> resource = MinecraftClient.getInstance().getResourceManager().getResource(new Identifier(ttFont.getNamespace(), "font/" + ttFont.getPath()));
			if (resource.isPresent()) {
				data = IOHelper.streamToArray(resource.get().getInputStream());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.ttfBuffer = data;
		this.oversample = fontAccess.getOversample();
		this.excludedCharacters = new ArrayList<>(fontAccess.getExcludedCharacters());
		this.shiftX = fontAccess.getShiftX();
		this.shiftY = fontAccess.getShiftY();
		this.scaleFactor = fontAccess.getScaleFactor();
		this.ascent = fontAccess.getAscent();
	}

	@Override
	public TrueTypeFont export(RegistryReader handler) {
		STBTTFontinfo sTBTTFontinfo = STBTTFontinfo.malloc();
		ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(this.ttfBuffer.length);
		byteBuffer2.put(this.ttfBuffer);
		byteBuffer2.flip();
		if (!STBTruetype.stbtt_InitFont(sTBTTFontinfo, byteBuffer2)) {
			try {
				throw new IOException("Invalid ttf");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		TrueTypeFont trueTypeFont = UnsafeHelper.allocateInstance(TrueTypeFont.class);
		TrueTypeFontAccessor trueTypeFontAccess = (TrueTypeFontAccessor) trueTypeFont;
		trueTypeFontAccess.setInfo(sTBTTFontinfo);
		trueTypeFontAccess.setOversample(this.oversample);
		trueTypeFontAccess.setBuffer(byteBuffer2);
		trueTypeFontAccess.setExcludedCharacters(new IntArraySet(this.excludedCharacters));
		trueTypeFontAccess.setShiftX(this.shiftX);
		trueTypeFontAccess.setShiftY(this.shiftY);
		trueTypeFontAccess.setScaleFactor(this.scaleFactor);
		trueTypeFontAccess.setAscent(this.ascent);
		return trueTypeFont;
	}
}
