package dev.quantumfusion.dashloader.mixin.accessor;

import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import net.minecraft.client.font.TrueTypeFont;
import org.lwjgl.stb.STBTTFontinfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrueTypeFont.class)
public interface TrueTypeFontAccessor {
	@Accessor
	@Mutable
	void setBuffer(ByteBuffer thing);

	@Accessor
	STBTTFontinfo getInfo();

	@Accessor
	@Mutable
	void setInfo(STBTTFontinfo thing);

	@Accessor
	float getOversample();

	@Accessor
	@Mutable
	void setOversample(float thing);

	@Accessor
	IntSet getExcludedCharacters();

	@Accessor
	@Mutable
	void setExcludedCharacters(IntSet thing);

	@Accessor
	float getShiftX();

	@Accessor
	@Mutable
	void setShiftX(float thing);

	@Accessor
	float getShiftY();

	@Accessor
	@Mutable
	void setShiftY(float thing);

	@Accessor
	float getScaleFactor();

	@Accessor
	@Mutable
	void setScaleFactor(float thing);

	@Accessor
	float getAscent();

	@Accessor
	@Mutable
	void setAscent(float thing);


}
