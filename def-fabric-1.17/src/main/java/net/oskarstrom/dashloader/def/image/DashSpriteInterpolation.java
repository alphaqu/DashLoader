package net.oskarstrom.dashloader.def.image;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.def.mixin.accessor.SpriteInterpolationAccessor;
import net.oskarstrom.dashloader.def.util.UnsafeHelper;

public class DashSpriteInterpolation {
	@Serialize(order = 0)
	public final int[] images;

	public DashSpriteInterpolation(@Deserialize("images") int[] images) {
		this.images = images;
	}

	public DashSpriteInterpolation(Sprite.Interpolation interpolation, DashRegistry registry) {
		final NativeImage[] imagesIn = ((SpriteInterpolationAccessor) (Object) interpolation).getImages();
		this.images = new int[imagesIn.length];
		for (int i = 0; i < imagesIn.length; i++) {
			this.images[i] = registry.add(imagesIn[i]);
		}

	}

	public final Sprite.Interpolation toUndash(final Sprite owner, final DashRegistry registry) {
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
