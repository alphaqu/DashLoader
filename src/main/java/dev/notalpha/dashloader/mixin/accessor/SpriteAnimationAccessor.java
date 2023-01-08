package dev.notalpha.dashloader.mixin.accessor;

import java.util.List;
import net.minecraft.client.texture.Sprite;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Sprite.Animation.class)
public interface SpriteAnimationAccessor {


	@Invoker("<init>")
	static Sprite.Animation init(Sprite parent, List<Sprite.AnimationFrame> list, int i, @Nullable Sprite.Interpolation interpolation) {
		throw new AssertionError();
	}

	@Accessor
	List<Sprite.AnimationFrame> getFrames();

	@Accessor
	int getFrameCount();

	@Accessor
	Sprite.Interpolation getInterpolation();

}
