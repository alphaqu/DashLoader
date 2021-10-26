package net.oskarstrom.dashloader.def.image;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.def.mixin.accessor.SpriteInterpolationAccessor;
import net.oskarstrom.dashloader.def.util.UnsafeHelper;
import net.oskarstrom.dashloader.def.util.mixins.SpriteInterpolationDuck;

@Data
public class DashSpriteInterpolation {
	public final int[] images;

	public DashSpriteInterpolation(int[] images) {
		this.images = images;
	}

	public DashSpriteInterpolation(Sprite.Interpolation interpolation, DashRegistry registry) {
		final NativeImage[] imagesIn = ((SpriteInterpolationAccessor) (Object) interpolation).getImages();
		this.images = new int[imagesIn.length];
		for (int i = 0; i < imagesIn.length; i++) {
			this.images[i] = registry.add(imagesIn[i]);
		}

	}

	public final Sprite.Interpolation toUndash(final Sprite owner, final DashExportHandler registry) {
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
