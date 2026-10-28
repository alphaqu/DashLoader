package dev.notalpha.dashloader.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import net.minecraft.client.renderer.texture.SpriteContents;

@Mixin(SpriteContents.AnimatedTexture.class)
public interface SpriteAnimationAccessor {
	// AnimatedTexture is a non-static inner class: the ctor takes the outer instance first.
	@Invoker("<init>")
	static SpriteContents.AnimatedTexture init(SpriteContents parent, List<SpriteContents.FrameInfo> frames, int frameRowSize, boolean interpolateFrames) {
		throw new AssertionError();
	}

	@Accessor
	List<SpriteContents.FrameInfo> getFrames();

	@Accessor
	int getFrameRowSize();

	@Accessor("interpolateFrames")
	boolean getInterpolation();
}
