package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.io.data.collection.IntIntList;
import dev.notalpha.dashloader.misc.UnsafeHelper;
import dev.notalpha.dashloader.mixin.accessor.UnicodeTextureFontAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import net.minecraft.client.font.UnicodeTextureFont;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class DashUnicodeFont implements DashObject<UnicodeTextureFont> {
	public final byte[] sizes;
	public final String template;
	public final IntIntList images;

	public DashUnicodeFont(byte[] sizes, String template, IntIntList images) {
		this.sizes = sizes;
		this.template = template;
		this.images = images;
	}

	public DashUnicodeFont(UnicodeTextureFont rawFont, RegistryWriter writer) {
		this.images = new IntIntList();
		UnicodeTextureFontAccessor font = ((UnicodeTextureFontAccessor) rawFont);
		font.getImages().forEach(
				(identifier, nativeImage) -> {
					this.images.put(writer.add(identifier), writer.add(nativeImage));
				}
		);

		this.sizes = font.getSizes();
		this.template = font.getTemplate();
	}


	public UnicodeTextureFont export(RegistryReader handler) {
		UnicodeTextureFont font = UnsafeHelper.allocateInstance(UnicodeTextureFont.class);
		UnicodeTextureFontAccessor accessor = ((UnicodeTextureFontAccessor) font);
		accessor.setSizes(this.sizes);
		accessor.setTemplate(this.template);

		Map<Identifier, NativeImage> images = new HashMap<>();
		this.images.forEach((key, value) ->  {
			images.put(handler.get(key), handler.get(value));
		});
		accessor.setImages(images);
		return font;
	}
}
