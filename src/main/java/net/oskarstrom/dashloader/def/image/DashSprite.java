package net.oskarstrom.dashloader.def.image;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.mixin.accessor.SpriteAccessor;
import net.oskarstrom.dashloader.def.util.UnsafeHelper;

@Data
public class DashSprite implements Dashable<Sprite> {
	@DataNullable
	public final DashSpriteAnimation animation;
	public final int x;
	public final int y;
	public final int width;
	public final int height;
	public final float uMin;
	public final float uMax;
	public final float vMin;
	public final float vMax;
	public int[] images;


	public DashSprite(
			DashSpriteAnimation animation,
			int x,
			int y,
			int width,
			int height,
			float uMin,
			float uMax,
			float vMin,
			float vMax,
			int[] images
	) {
		this.animation = animation;
		this.images = images;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.uMin = uMin;
		this.uMax = uMax;
		this.vMin = vMin;
		this.vMax = vMax;
	}

	public DashSprite(Sprite sprite, DashRegistry registry) {
		final NativeImage[] images = ((SpriteAccessor) sprite).getImages();
		this.images = new int[images.length];
		for (int i = 0; i < images.length; i++) {
			this.images[i] = registry.add(images[i]);
		}
		x = sprite.getX();
		y = sprite.getY();
		width = sprite.getWidth();
		height = sprite.getHeight();
		uMin = sprite.getMinU();
		uMax = sprite.getMaxU();
		vMin = sprite.getMinV();
		vMax = sprite.getMaxV();
		this.animation = DashHelper.nullable((Sprite.Animation) sprite.getAnimation(), animation1 -> new DashSpriteAnimation(animation1, registry));
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
