package dev.quantumfusion.dashloader.def.mixin.accessor;

import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Sprite.AnimationFrame.class)
public interface SpriteAnimationFrameAccessor {

	@Invoker("<init>")
	static Sprite.AnimationFrame newSpriteFrame(int index, int time) {
		throw new AssertionError();
	}

	@Accessor
	int getIndex();

	@Accessor
	int getTime();
}
