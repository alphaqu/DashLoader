package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAccessor;
import dev.quantumfusion.dashloader.def.util.UnsafeHelper;

@Data
public record DashSprite(@DataNullable DashSpriteAnimation animation, int[] images,
						 int x, int y, int width, int height, float uMin, float uMax, float vMin,
						 float vMax) implements Dashable<Sprite> {

	public DashSprite(Sprite sprite, DashRegistry registry) {
		this(DashHelper.nullable((Sprite.Animation) sprite.getAnimation(), animation1 -> new DashSpriteAnimation(animation1, registry)), convertImages((SpriteAccessor) sprite, registry), sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight(), sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
	}
	private static int[] convertImages(SpriteAccessor sprite, DashRegistry registry) {
		final NativeImage[] images = sprite.getImages();
		var imageOut = new int[images.length];
		for (int i = 0; i < images.length; i++) {
			imageOut[i] = registry.add(images[i]);
		}
		return imageOut;
	}

	@Override
	public final Sprite toUndash(final DashExportHandler registry) {
		final Sprite out = UnsafeHelper.allocateInstance(Sprite.class);
		final SpriteAccessor spriteAccessor = ((SpriteAccessor) out);
		final NativeImage[] imagesOut = new NativeImage[images.length];
		for (int i = 0; i < images.length; i++) {
			imagesOut[i] = registry.get(images[i]);
		}
		spriteAccessor.setImages(imagesOut);
		spriteAccessor.setX(x);
		spriteAccessor.setY(y);
		spriteAccessor.setWidth(width);
		spriteAccessor.setHeight(height);
		spriteAccessor.setUMin(uMin);
		spriteAccessor.setUMax(uMax);
		spriteAccessor.setVMin(vMin);
		spriteAccessor.setVMax(vMax);
		spriteAccessor.setAnimation(DashHelper.nullable(animation, animation -> animation.toUndash(out, registry)));
		return out;
	}


}
