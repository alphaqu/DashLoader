package net.oskarstrom.dashloader.def.image;

import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.def.mixin.accessor.SpriteAnimationFrameAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.texture.Sprite;
import net.oskarstrom.dashloader.core.registry.DashRegistry;

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
	public Sprite.AnimationFrame toUndash(DashExportHandler exportHandler) {
		return SpriteAnimationFrameAccessor.newSpriteFrame(index, time);
	}
}
