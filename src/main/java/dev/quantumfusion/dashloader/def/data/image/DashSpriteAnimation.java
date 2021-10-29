package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.texture.Sprite;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAnimationAccessor;

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


	public DashSpriteAnimation(Sprite.Animation animation, DashRegistry registry) {
		SpriteAnimationAccessor access = ((SpriteAnimationAccessor) animation);
		frames = DashHelper.convertCollection(access.getFrames(), DashSpriteAnimationFrame::new);
		frameCount = access.getFrameCount();
		interpolation = DashHelper.nullable(access.getInterpolation(), registry, DashSpriteInterpolation::new);
	}


	public Sprite.Animation toUndash(Sprite owner, DashExportHandler registry) {
		return SpriteAnimationAccessor.init(
				owner,
				DashHelper.convertCollection(frames, frame -> frame.toUndash(registry)),
				frameCount,
				DashHelper.nullable(interpolation, interpolation -> interpolation.toUndash(owner, registry))
		);
	}
}
