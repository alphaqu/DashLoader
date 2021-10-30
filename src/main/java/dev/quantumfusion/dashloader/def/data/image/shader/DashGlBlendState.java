package dev.quantumfusion.dashloader.def.data.image.shader;

import dev.quantumfusion.dashloader.def.mixin.accessor.GlBlendStateAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.gl.GlBlendState;

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

	public GlBlendState export() {
		return GlBlendStateAccessor.create(separateBlend, blendDisabled, srcRgb, dstRgb, srcAlpha, dstAlpha, func);
	}
}
