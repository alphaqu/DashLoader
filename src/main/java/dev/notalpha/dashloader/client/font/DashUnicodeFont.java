package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.RegistryReader;
import dev.notalpha.dashloader.api.RegistryWriter;
import dev.notalpha.dashloader.misc.UnsafeHelper;
import dev.notalpha.dashloader.mixin.accessor.FontImageAccessor;
import dev.notalpha.dashloader.mixin.accessor.UnicodeTextureFontAccessor;
import dev.quantumfusion.hyphen.scan.annotations.DataFixedArraySize;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.font.UnicodeTextureFont;

public final class DashUnicodeFont implements DashObject<UnicodeTextureFont> {


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
			this.images[i] = fontImage == null ? null : writer.add(((FontImageAccessor) fontImage).getImage());
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
			fontImages[i] = image == null ? null : FontImageAccessor.create(this.sizes, handler.get(image));
		}
		accessor.setFontImages(fontImages);
		return font;
	}
}
