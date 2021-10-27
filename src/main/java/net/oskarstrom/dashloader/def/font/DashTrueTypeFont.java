package net.oskarstrom.dashloader.def.font;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TrueTypeFont;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.mixin.accessor.TrueTypeFontAccessor;
import net.oskarstrom.dashloader.def.util.IOHelper;
import net.oskarstrom.dashloader.def.util.UnsafeHelper;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;

@Data
@DashObject(TrueTypeFont.class)
public class DashTrueTypeFont implements DashFont {
	public final byte[] ttfBuffer;
	public final float oversample;
	public final Set<Integer> excludedCharacters;
	public final float shiftX;
	public final float shiftY;
	public final float scaleFactor;
	public final float ascent;


	public DashTrueTypeFont(byte[] ttfBuffer, float oversample, Set<Integer> excludedCharacters, float shiftX, float shiftY, float scaleFactor, float ascent) {
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
		final Object2ObjectMap<STBTTFontinfo, Identifier> fontData = DashLoader.getVanillaData().getFontData();
		byte[] data = null;
		try {
			Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(new Identifier(fontData.get(fontAccess.getInfo()).getNamespace(), "font/" + fontData.get(fontAccess.getInfo()).getPath()));
			data = IOHelper.streamToArray(resource.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		ttfBuffer = data;
		oversample = fontAccess.getOversample();
		excludedCharacters = fontAccess.getExcludedCharacters();
		shiftX = fontAccess.getShiftX();
		shiftY = fontAccess.getShiftY();
		scaleFactor = fontAccess.getScaleFactor();
		ascent = fontAccess.getAscent();
	}

	@Override
	public TrueTypeFont toUndash(DashExportHandler handler) {
		STBTTFontinfo sTBTTFontinfo = STBTTFontinfo.malloc();
		ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(ttfBuffer.length);
		byteBuffer2.put(ttfBuffer);
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
		trueTypeFontAccess.setOversample(oversample);
		trueTypeFontAccess.setBuffer(byteBuffer2);
		trueTypeFontAccess.setExcludedCharacters(new IntArraySet(excludedCharacters));
		trueTypeFontAccess.setShiftX(shiftX);
		trueTypeFontAccess.setShiftY(shiftY);
		trueTypeFontAccess.setScaleFactor(scaleFactor);
		trueTypeFontAccess.setAscent(ascent);
		return trueTypeFont;
	}
}
