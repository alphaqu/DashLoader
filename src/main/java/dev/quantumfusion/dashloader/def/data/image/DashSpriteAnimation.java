package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.core.util.DashUtil;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAnimationAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.Sprite.AnimationFrame;
import java.util.ArrayList;
import java.util.List;

@Data
public class DashSpriteAnimation {
	public final List<DashSpriteAnimationFrame> frames;
	public final int frameCount;
	@DataNullable
	public final DashSpriteInterpolation interpolation;

	public DashSpriteAnimation(
			List<DashSpriteAnimationFrame> frames,
			int frameCount,
			DashSpriteInterpolation interpolation) {
		this.frames = frames;
		this.frameCount = frameCount;
		this.interpolation = interpolation;
	}


	public DashSpriteAnimation(Sprite.Animation animation, RegistryWriter registry) {
		SpriteAnimationAccessor access = ((SpriteAnimationAccessor) animation);
		this.frames = new ArrayList<>();
		for (var frame : access.getFrames()) {
			this.frames.add(new DashSpriteAnimationFrame(frame));
		}
		this.frameCount = access.getFrameCount();
		this.interpolation = DashUtil.nullable(access.getInterpolation(), registry, DashSpriteInterpolation::new);
	}


	public Sprite.Animation export(Sprite owner, RegistryReader registry) {
		var framesOut = new ArrayList<AnimationFrame>();
		for (var frame : this.frames) {
			framesOut.add(frame.export(registry));
		}

		return SpriteAnimationAccessor.init(
				owner,
				framesOut,
				frameCount,
				DashUtil.nullable(interpolation, interpolation -> interpolation.export(owner, registry))
		);
	}
}
