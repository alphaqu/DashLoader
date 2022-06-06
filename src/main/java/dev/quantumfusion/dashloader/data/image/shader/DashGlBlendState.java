package dev.quantumfusion.dashloader.data.image.shader;

import dev.quantumfusion.dashloader.mixin.accessor.GlBlendStateAccessor;
import net.minecraft.client.gl.GlBlendState;

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
		return GlBlendStateAccessor.create(this.separateBlend, this.blendDisabled, this.srcRgb, this.dstRgb, this.srcAlpha, this.dstAlpha, this.func);
	}
}
