package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteInterpolationAccessor;
import dev.quantumfusion.dashloader.def.util.UnsafeHelper;
import dev.quantumfusion.dashloader.def.util.mixins.SpriteInterpolationDuck;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;

@Data
public class DashSpriteInterpolation {
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

	public final Sprite.Interpolation export(final Sprite owner, final RegistryReader registry) {
		final Sprite.Interpolation spriteInterpolation = UnsafeHelper.allocateInstance(Sprite.Interpolation.class);
		final SpriteInterpolationAccessor spriteInterpolationAccessor = ((SpriteInterpolationAccessor) (Object) spriteInterpolation);
		final NativeImage[] nativeImages = new NativeImage[images.length];
		for (int i = 0; i < images.length; i++) {
			nativeImages[i] = registry.get(images[i]);
		}
		spriteInterpolationAccessor.setImages(nativeImages);
		((SpriteInterpolationDuck) (Object) spriteInterpolation).interpolation(owner);
		return spriteInterpolation;
	}
}
