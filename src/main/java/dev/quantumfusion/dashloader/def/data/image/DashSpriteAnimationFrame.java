package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAnimationFrameAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.texture.Sprite;

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
	public Sprite.AnimationFrame export(RegistryReader exportHandler) {
		return SpriteAnimationFrameAccessor.newSpriteFrame(index, time);
	}
}
