package dev.notalpha.dashloader.minecraft.sprite;

import dev.notalpha.dashloader.mixin.accessor.SpriteContentsAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import dev.notalpha.dashloader.util.UnsafeHelper;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import org.jetbrains.annotations.Nullable;

public final class DashSpriteContents {
	public final int id;
	public final int image;
	@Nullable
	@DataNullable
	public final DashSpriteAnimation animation;

	public final int width;
	public final int height;
	public final int mipMaps;

	public DashSpriteContents(int id, int image, @Nullable DashSpriteAnimation animation, int width, int height, int mipMaps) {
		this.id = id;
		this.image = image;
		this.animation = animation;
		this.width = width;
		this.height = height;
		this.mipMaps = mipMaps;
	}

	public DashSpriteContents(SpriteContents contents, RegistryWriter writer) {
		var access = (SpriteContentsAccessor) contents;
		this.id = writer.add(contents.getId());
		this.image = writer.add(access.getImage());
		this.width = contents.getWidth();
		this.height = contents.getHeight();
		this.mipMaps = access.getMipmapLevelsImages().length - 1;
		SpriteContents.Animation animation = access.getAnimation();
		this.animation = animation == null ? null : new DashSpriteAnimation(animation);
	}

	public SpriteContents export(RegistryReader reader) {
		final SpriteContents out = UnsafeHelper.allocateInstance(SpriteContents.class);
		var access = (SpriteContentsAccessor) out;
		access.setId(reader.get(this.id));

		NativeImage image = reader.get(this.image);
		access.setImage(image);
		access.setHeight(height);
		access.setWidth(width);
		access.setMipmapLevelsImages(new NativeImage[]{image});
		access.setAnimation(this.animation == null ? null : animation.export(out, reader));
		return out;
	}
}
