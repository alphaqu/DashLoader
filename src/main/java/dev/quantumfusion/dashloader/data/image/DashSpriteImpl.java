package dev.quantumfusion.dashloader.data.image;

import dev.quantumfusion.dashloader.api.DashDependencies;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.mixin.accessor.MipmapHelperAccessor;
import dev.quantumfusion.dashloader.mixin.accessor.SpriteAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.util.DashUtil;
import dev.quantumfusion.dashloader.util.UnsafeHelper;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;

@DashObject(Sprite.class)
@DashDependencies(DashImage.class)
public class DashSpriteImpl implements DashSprite {
	@DataNullable
	public final DashSpriteAnimation animation;
	public final int image;
	public final boolean imageTransparent;
	public final int images;
	public final int x;
	public final int y;
	public final int width;
	public final int height;
	public final float uMin;
	public final float uMax;
	public final float vMin;
	public final float vMax;

	public DashSpriteImpl(DashSpriteAnimation animation,
						  int image,
						  boolean imageTransparent,
						  int images,
						  int x, int y, int width, int height,
						  float uMin, float uMax, float vMin, float vMax
	) {
		this.animation = animation;
		this.image = image;
		this.imageTransparent = imageTransparent;
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
		this.animation = DashUtil.nullable(sprite.getAnimation(), animation -> new DashSpriteAnimation((Sprite.Animation) animation, writer));

		NativeImage[] images = ((SpriteAccessor) sprite).getImages();
		NativeImage image = images[0];
		this.image = writer.add(image);

		boolean transparent = false;
		check:
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				if (image.getColor(x, y) >> 24 == 0) {
					transparent = true;
					break check;
				}
			}
		}
		this.imageTransparent = transparent;
		this.images = images.length;

		this.x = sprite.getX();
		this.y = sprite.getY();
		this.width = sprite.getWidth();
		this.height = sprite.getHeight();
		this.uMin = sprite.getMinU();
		this.uMax = sprite.getMaxU();
		this.vMin = sprite.getMinV();
		this.vMax = sprite.getMaxV();
	}

	@Override
	public Sprite export(final RegistryReader registry) {
		final Sprite out = UnsafeHelper.allocateInstance(Sprite.class);
		final SpriteAccessor spriteAccessor = ((SpriteAccessor) out);


		final NativeImage[] images = new NativeImage[this.images];
		images[0] = registry.get(this.image);
		for (int i = 1; i <= (this.images - 1); ++i) {
			NativeImage oldLevel = images[i - 1];
			NativeImage newLevel = new NativeImage(oldLevel.getWidth() >> 1, oldLevel.getHeight() >> 1, false);
			int width = newLevel.getWidth();
			int height = newLevel.getHeight();

			for (int x = 0; x < width; ++x) {
				for (int y = 0; y < height; ++y) {
					newLevel.setColor(x, y, MipmapHelperAccessor.blend(oldLevel.getColor(x * 2, y * 2), oldLevel.getColor(x * 2 + 1, y * 2), oldLevel.getColor(x * 2, y * 2 + 1), oldLevel.getColor(x * 2 + 1, y * 2 + 1), this.imageTransparent));
				}
			}

			images[i] = newLevel;
		}

		spriteAccessor.setImages(images);
		spriteAccessor.setX(this.x);
		spriteAccessor.setY(this.y);
		spriteAccessor.setWidth(this.width);
		spriteAccessor.setHeight(this.height);
		spriteAccessor.setUMin(this.uMin);
		spriteAccessor.setUMax(this.uMax);
		spriteAccessor.setVMin(this.vMin);
		spriteAccessor.setVMax(this.vMax);
		spriteAccessor.setAnimation(DashUtil.nullable(this.animation, animation -> animation.export(out, registry)));
		return out;
	}
}
