package dev.quantumfusion.dashloader.data.image;

import dev.quantumfusion.dashloader.mixin.accessor.SpriteInterpolationAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.util.UnsafeHelper;
import dev.quantumfusion.dashloader.util.mixins.SpriteInterpolationDuck;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;

public final class DashSpriteInterpolation {
	public final int[] images;

	public DashSpriteInterpolation(int[] images) {
		this.images = images;
	}

	public DashSpriteInterpolation(Sprite.Interpolation interpolation, RegistryWriter registry) {
		final NativeImage[] imagesIn = ((SpriteInterpolationAccessor) (Object) interpolation).getImages();
		this.images = new int[imagesIn.length];
		for (int i = 0; i < imagesIn.length; i++) {
			this.images[i] = registry.add(imagesIn[i]);
		}

	}

	@SuppressWarnings("ConstantConditions")
	public Sprite.Interpolation export(final Sprite owner, final RegistryReader registry) {
		final Sprite.Interpolation spriteInterpolation = UnsafeHelper.allocateInstance(Sprite.Interpolation.class);
		final SpriteInterpolationAccessor spriteInterpolationAccessor = ((SpriteInterpolationAccessor) (Object) spriteInterpolation);
		final NativeImage[] nativeImages = new NativeImage[this.images.length];
		for (int i = 0; i < this.images.length; i++) {
			nativeImages[i] = registry.get(this.images[i]);
		}
		spriteInterpolationAccessor.setImages(nativeImages);
		((SpriteInterpolationDuck) (Object) spriteInterpolation).interpolation(owner);
		return spriteInterpolation;
	}
}
