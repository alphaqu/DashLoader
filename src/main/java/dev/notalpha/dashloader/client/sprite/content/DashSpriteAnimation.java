package dev.notalpha.dashloader.client.sprite.content;

import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.mixin.accessor.SpriteAnimationAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.texture.SpriteContents;

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

	public DashSpriteAnimation(SpriteContents.AnimatedTexture animation) {
		SpriteAnimationAccessor access = ((SpriteAnimationAccessor) animation);
		this.frames = new ArrayList<>();
		for (var frame : access.getFrames()) {
			this.frames.add(new DashSpriteAnimationFrame(frame));
		}
		this.frameCount = access.getFrameRowSize();
		this.interpolation = access.getInterpolation();
	}

	public SpriteContents.AnimatedTexture export(SpriteContents owner, RegistryReader registry) {
		var framesOut = new ArrayList<SpriteContents.FrameInfo>();
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashSpriteAnimation that = (DashSpriteAnimation) o;

		if (frameCount != that.frameCount) return false;
		if (interpolation != that.interpolation) return false;
		return frames.equals(that.frames);
	}

	@Override
	public int hashCode() {
		int result = frames.hashCode();
		result = 31 * result + frameCount;
		result = 31 * result + (interpolation ? 1 : 0);
		return result;
	}
}
