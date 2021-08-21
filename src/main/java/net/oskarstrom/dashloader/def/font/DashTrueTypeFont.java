package net.oskarstrom.dashloader.def.font;

import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.mixin.accessor.TrueTypeFontAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TrueTypeFont;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.util.IOHelper;
import net.oskarstrom.dashloader.def.util.UnsafeHelper;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;

@DashObject(TrueTypeFont.class)
public class DashTrueTypeFont implements DashFont {
	@Serialize(order = 0)
	public final byte[] ttfBuffer;
	@Serialize(order = 1)
	public final float oversample;
	@Serialize(order = 2)
	public final Set<Integer> excludedCharacters;
	@Serialize(order = 3)
	public final float shiftX;
	@Serialize(order = 4)
	public final float shiftY;
	@Serialize(order = 5)
	public final float scaleFactor;
	@Serialize(order = 6)
	public final float ascent;


	public DashTrueTypeFont(@Deserialize("ttfBuffer") byte[] ttfBuffer,
							@Deserialize("oversample") float oversample,
							@Deserialize("excludedCharacters") Set<Integer> excludedCharacters,
							@Deserialize("shiftX") float shiftX,
							@Deserialize("shiftY") float shiftY,
							@Deserialize("scaleFactor") float scaleFactor,
							@Deserialize("ascent") float ascent) {
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
	public TrueTypeFont toUndash(DashRegistry registry) {
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
