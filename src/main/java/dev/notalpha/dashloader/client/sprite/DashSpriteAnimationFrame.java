package dev.notalpha.dashloader.client.sprite;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.mixin.accessor.SpriteAnimationFrameAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import net.minecraft.client.texture.Sprite;

public final class DashSpriteAnimationFrame implements DashObject<Sprite.AnimationFrame> {
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
	public Sprite.AnimationFrame export(RegistryReader reader) {
		return SpriteAnimationFrameAccessor.newSpriteFrame(this.index, this.time);
	}
}
