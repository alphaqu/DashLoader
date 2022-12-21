package dev.quantumfusion.dashloader.data.font;

import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.data.common.IntIntList;
import dev.quantumfusion.dashloader.mixin.accessor.FontImageAccessor;
import dev.quantumfusion.dashloader.mixin.accessor.UnicodeTextureFontAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.util.DashUtil;
import dev.quantumfusion.dashloader.util.UnsafeHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.quantumfusion.hyphen.scan.annotations.DataFixedArraySize;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.font.UnicodeTextureFont;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

@DashObject(UnicodeTextureFont.class)
public final class DashUnicodeFont implements DashFont {


	public final @DataNullable Integer @DataFixedArraySize(256) [] images;
	public final byte[] sizes;

	public DashUnicodeFont(Integer[] images, byte[] sizes) {
		this.images = images;
		this.sizes = sizes;
	}

	public DashUnicodeFont(UnicodeTextureFont rawFont, RegistryWriter writer) {
		this.images = new Integer[256];
		UnicodeTextureFontAccessor font = ((UnicodeTextureFontAccessor) rawFont);
		UnicodeTextureFont.FontImage[] fontImages = font.getFontImages();
		for (int i = 0; i < fontImages.length; i++) {
			UnicodeTextureFont.FontImage fontImage = fontImages[i];
			this.images[i] = DashUtil.nullable(fontImage, image -> writer.add(((FontImageAccessor) image).getImage()));
		}
		this.sizes = font.getSizes();
	}


	public UnicodeTextureFont export(RegistryReader handler) {
		UnicodeTextureFont font = UnsafeHelper.allocateInstance(UnicodeTextureFont.class);
		UnicodeTextureFontAccessor accessor = ((UnicodeTextureFontAccessor) font);
		accessor.setSizes(this.sizes);
		UnicodeTextureFont.FontImage[] fontImages = new UnicodeTextureFont.FontImage[256];

		for (int i = 0; i < images.length; i++) {
			Integer image = images[i];
			fontImages[i] = DashUtil.nullable(image, img -> FontImageAccessor.create(this.sizes, handler.get(img)));
		}
		accessor.setFontImages(fontImages);
		return font;
	}
}
