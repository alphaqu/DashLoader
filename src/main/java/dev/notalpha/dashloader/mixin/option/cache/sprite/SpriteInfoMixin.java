package dev.notalpha.dashloader.mixin.option.cache.sprite;

import dev.notalpha.dashloader.util.mixins.SpriteInfoDuck;
import net.minecraft.client.texture.Sprite;

public class SpriteInfoMixin implements SpriteInfoDuck {
	private Sprite cached;

	@Override
	public Sprite getCached() {
		return this.cached;
	}

	@Override
	public void setCached(Sprite cached) {
		this.cached = cached;
	}
}
