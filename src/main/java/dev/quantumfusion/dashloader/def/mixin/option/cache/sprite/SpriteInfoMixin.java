package dev.quantumfusion.dashloader.def.mixin.option.cache.sprite;

import dev.quantumfusion.dashloader.def.util.mixins.SpriteInfoDuck;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Sprite.Info.class)
public class SpriteInfoMixin implements SpriteInfoDuck {
	private Sprite cached;

	@Override
	public Sprite getCached() {
		return cached;
	}

	@Override
	public void setCached(Sprite cached) {
		this.cached = cached;
	}
}
