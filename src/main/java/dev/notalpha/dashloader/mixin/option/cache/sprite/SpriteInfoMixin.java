package dev.notalpha.dashloader.mixin.option.cache.sprite;

import dev.notalpha.dashloader.misc.duck.SpriteInfoDuck;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Sprite.Info.class)
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
