package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.texture.Sprite;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAnimationFrameAccessor;

@Data
public class DashSpriteAnimationFrame implements Dashable<Sprite.AnimationFrame> {
	public final int index;
	public final int time;

	public DashSpriteAnimationFrame(int index, int time) {
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
