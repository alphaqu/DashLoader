package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.dashloader.core.api.DashDependencies;
import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.core.util.DashUtil;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAccessor;
import dev.quantumfusion.dashloader.def.util.UnsafeHelper;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;

@Data
@DashObject(Sprite.class)
@DashDependencies(DashImage.class)
public class DashSpriteImpl implements DashSprite {
	@DataNullable
	public final DashSpriteAnimation animation;
	public final int[] images;
	public final int x;
	public final int y;
	public final int width;
	public final int height;
	public final float uMin;
	public final float uMax;
	public final float vMin;
	public final float vMax;

	public DashSpriteImpl(DashSpriteAnimation animation, int[] images,
			int x, int y, int width, int height, float uMin, float uMax, float vMin,
			float vMax) {
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

	public DashSpriteImpl(Sprite sprite, RegistryWriter writer) {
		this(DashUtil.nullable((Sprite.Animation) sprite.getAnimation(), animation1 -> new DashSpriteAnimation(animation1, writer)),
			 convertImages((SpriteAccessor) sprite, writer), sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight(), sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
	}

	private static int[] convertImages(SpriteAccessor sprite, RegistryWriter writer) {
		final NativeImage[] images = sprite.getImages();
		var imageOut = new int[images.length];
		for (int i = 0; i < images.length; i++) {
			imageOut[i] = writer.add(images[i]);
		}
		return imageOut;
	}

	@Override
	public Sprite export(final RegistryReader registry) {
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
		spriteAccessor.setAnimation(DashUtil.nullable(animation, animation -> animation.export(out, registry)));
		return out;
	}
}
