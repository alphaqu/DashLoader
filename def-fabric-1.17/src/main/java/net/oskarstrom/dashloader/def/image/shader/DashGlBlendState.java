package net.oskarstrom.dashloader.def.image.shader;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.gl.GlBlendState;
import net.oskarstrom.dashloader.def.mixin.accessor.GlBlendStateAccessor;

public class DashGlBlendState {
	@Serialize(order = 0)
	public final int srcRgb;
	@Serialize(order = 1)
	public final int srcAlpha;
	@Serialize(order = 2)
	public final int dstRgb;
	@Serialize(order = 3)
	public final int dstAlpha;
	@Serialize(order = 4)
	public final int func;
	@Serialize(order = 5)
	public final boolean separateBlend;
	@Serialize(order = 6)
	public final boolean blendDisabled;

	public DashGlBlendState(@Deserialize("srcRgb") int srcRgb,
							@Deserialize("srcAlpha") int srcAlpha,
							@Deserialize("dstRgb") int dstRgb,
							@Deserialize("dstAlpha") int dstAlpha,
							@Deserialize("func") int func,
							@Deserialize("separateBlend") boolean separateBlend,
							@Deserialize("blendDisabled") boolean blendDisabled) {
		this.srcRgb = srcRgb;
		this.srcAlpha = srcAlpha;
		this.dstRgb = dstRgb;
		this.dstAlpha = dstAlpha;
		this.func = func;
		this.separateBlend = separateBlend;
		this.blendDisabled = blendDisabled;
	}

	public DashGlBlendState(GlBlendState blendState) {
		GlBlendStateAccessor blendStateAccess = (GlBlendStateAccessor) blendState;
		this.srcRgb = blendStateAccess.getSrcRgb();
		this.srcAlpha = blendStateAccess.getSrcAlpha();
		this.dstRgb = blendStateAccess.getDstRgb();
		this.dstAlpha = blendStateAccess.getDstAlpha();
		this.func = blendStateAccess.getFunc();
		this.separateBlend = blendStateAccess.getSeparateBlend();
		this.blendDisabled = blendStateAccess.getBlendDisabled();
	}

	public GlBlendState toUndash() {
		return GlBlendStateAccessor.create(separateBlend, blendDisabled, srcRgb, dstRgb, srcAlpha, dstAlpha, func);
	}
}
