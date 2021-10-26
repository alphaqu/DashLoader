package net.oskarstrom.dashloader.def.image.shader;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.gl.GlBlendState;
import net.oskarstrom.dashloader.def.mixin.accessor.GlBlendStateAccessor;

@Data
public record DashGlBlendState(
		int srcRgb, int srcAlpha, int dstRgb, int dstAlpha, int func, boolean separateBlend, boolean blendDisabled) {


	public DashGlBlendState(GlBlendStateAccessor blendStateAccess) {
		this(
				blendStateAccess.getSrcRgb(),
				blendStateAccess.getSrcAlpha(),
				blendStateAccess.getDstRgb(),
				blendStateAccess.getDstAlpha(),
				blendStateAccess.getFunc(),
				blendStateAccess.getSeparateBlend(),
				blendStateAccess.getBlendDisabled());
	}

	public DashGlBlendState(GlBlendState blendState) {
		this((GlBlendStateAccessor) blendState);
	}

	public GlBlendState toUndash() {
		return GlBlendStateAccessor.create(separateBlend, blendDisabled, srcRgb, dstRgb, srcAlpha, dstAlpha, func);
	}
}
