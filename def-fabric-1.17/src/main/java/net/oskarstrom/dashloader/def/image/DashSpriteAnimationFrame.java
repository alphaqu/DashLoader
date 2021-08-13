package net.oskarstrom.dashloader.def.image;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.texture.Sprite;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.def.mixin.accessor.SpriteAnimationFrameAccessor;

public class DashSpriteAnimationFrame implements Dashable<Sprite.AnimationFrame> {
	@Serialize(order = 0)
	public final int index;
	@Serialize(order = 1)
	public final int time;

	public DashSpriteAnimationFrame(@Deserialize("index") int index,
									@Deserialize("time") int time) {
		this.index = index;
		this.time = time;
	}

	public DashSpriteAnimationFrame(Sprite.AnimationFrame animationFrame) {
		SpriteAnimationFrameAccessor access = ((SpriteAnimationFrameAccessor) animationFrame);
		index = access.getIndex();
		time = access.getTime();
	}

	@Override
	public Sprite.AnimationFrame toUndash(DashRegistry registry) {
		return SpriteAnimationFrameAccessor.newSpriteFrame(index, time);
	}
}
