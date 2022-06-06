package dev.quantumfusion.dashloader.data.image;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.mixin.accessor.SpriteAnimationFrameAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import net.minecraft.client.texture.Sprite;

public class DashSpriteAnimationFrame implements Dashable<Sprite.AnimationFrame> {
	public final int index;
	public final int time;

	public DashSpriteAnimationFrame(int index, int time) {
		this.index = index;
		this.time = time;
	}

	public DashSpriteAnimationFrame(Sprite.AnimationFrame animationFrame) {
		SpriteAnimationFrameAccessor access = ((SpriteAnimationFrameAccessor) animationFrame);
		this.index = access.getIndex();
		this.time = access.getTime();
	}

	@Override
	public Sprite.AnimationFrame export(RegistryReader exportHandler) {
		return SpriteAnimationFrameAccessor.newSpriteFrame(this.index, this.time);
	}
}
