package net.oskarstrom.dashloader.def.image;

import net.oskarstrom.dashloader.def.mixin.accessor.SpriteAnimationAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;
import net.minecraft.client.texture.Sprite;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;

import java.util.ArrayList;
import java.util.List;

import static net.oskarstrom.dashloader.core.util.DashHelper.nullable;

public class DashSpriteAnimation {
	@Serialize(order = 0)
	public final List<DashSpriteAnimationFrame> frames;
	@Serialize(order = 1)
	public final int frameCount;
	@Serialize(order = 2)
	@SerializeNullable
	public final DashSpriteInterpolation interpolation;

	public DashSpriteAnimation(@Deserialize("frames") List<DashSpriteAnimationFrame> frames,
							   @Deserialize("frameCount") int frameCount,
							   @Deserialize("interpolation") DashSpriteInterpolation interpolation) {
		this.frames = frames;
		this.frameCount = frameCount;
		this.interpolation = interpolation;
	}


	public DashSpriteAnimation(Sprite.Animation animation, DashRegistry registry) {
		SpriteAnimationAccessor access = ((SpriteAnimationAccessor) animation);
		frames = convertList(access.getFrames(), DashSpriteAnimationFrame::new);
		frameCount = access.getFrameCount();
		interpolation = DashHelper.nullable(access.getInterpolation(), registry, DashSpriteInterpolation::new);
	}


	public Sprite.Animation toUndash(Sprite owner, DashRegistry registry) {
		return SpriteAnimationAccessor.init(
				owner,
				(ArrayList<?>) DashHelper.convertCollection(frames, frame -> frame.toUndash(registry)),
				frameCount,
				DashHelper.nullable(interpolation, interpolation -> interpolation.toUndash(owner, registry))
		);
	}
}
