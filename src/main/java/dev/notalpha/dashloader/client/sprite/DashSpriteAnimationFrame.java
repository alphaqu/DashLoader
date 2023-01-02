package dev.notalpha.dashloader.client.sprite;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.mixin.accessor.SpriteAnimationFrameAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import net.minecraft.client.texture.SpriteContents;

public final class DashSpriteAnimationFrame implements DashObject<SpriteContents.AnimationFrame> {
	public final int index;
	public final int time;

	public DashSpriteAnimationFrame(int index, int time) {
		this.index = index;
		this.time = time;
	}

	public DashSpriteAnimationFrame(SpriteContents.AnimationFrame animationFrame) {
		SpriteAnimationFrameAccessor access = ((SpriteAnimationFrameAccessor) animationFrame);
		this.index = access.getIndex();
		this.time = access.getTime();
	}

	@Override
	public SpriteContents.AnimationFrame export(RegistryReader exportHandler) {
		return SpriteAnimationFrameAccessor.newSpriteFrame(this.index, this.time);
	}
}
