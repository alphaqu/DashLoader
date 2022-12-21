package dev.quantumfusion.dashloader.mixin.option.cache.sprite;

import dev.quantumfusion.dashloader.util.mixins.SpriteInfoDuck;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;

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
