package dev.quantumfusion.dashloader.data.image;

import dev.quantumfusion.dashloader.mixin.accessor.SpriteAnimationAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import net.minecraft.client.texture.SpriteContents;

import java.util.ArrayList;
import java.util.List;

public final class DashSpriteAnimation {
	public final List<DashSpriteAnimationFrame> frames;
	public final int frameCount;
	public final boolean interpolation;

	public DashSpriteAnimation(
			List<DashSpriteAnimationFrame> frames,
			int frameCount,
			boolean interpolation) {
		this.frames = frames;
		this.frameCount = frameCount;
		this.interpolation = interpolation;
	}


	public DashSpriteAnimation(SpriteContents.Animation animation) {
		SpriteAnimationAccessor access = ((SpriteAnimationAccessor) animation);
		this.frames = new ArrayList<>();
		for (var frame : access.getFrames()) {
			this.frames.add(new DashSpriteAnimationFrame(frame));
		}
		this.frameCount = access.getFrameCount();
		this.interpolation = access.getInterpolation();
	}


	public SpriteContents.Animation export(SpriteContents owner, RegistryReader registry) {
		var framesOut = new ArrayList<SpriteContents.AnimationFrame>();
		for (var frame : this.frames) {
			framesOut.add(frame.export(registry));
		}

		return SpriteAnimationAccessor.init(
				owner,
				framesOut,
				this.frameCount,
				this.interpolation
		);
	}
}
