package dev.quantumfusion.dashloader.data.font;

import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.common.IntIntList;
import dev.quantumfusion.dashloader.mixin.accessor.UnicodeTextureFontAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.util.UnsafeHelper;
import net.minecraft.client.font.UnicodeTextureFont;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@DashObject(UnicodeTextureFont.class)
public class DashUnicodeFont implements DashFont {
	public final IntIntList images;
	public final byte[] sizes;
	public final String template;

	public DashUnicodeFont(IntIntList images, byte[] sizes, String template) {
		this.images = images;
		this.sizes = sizes;
		this.template = template;
	}

	public DashUnicodeFont(UnicodeTextureFont rawFont, RegistryWriter writer) {
		this.images = new IntIntList(new ArrayList<>());
		UnicodeTextureFontAccessor font = ((UnicodeTextureFontAccessor) rawFont);
		font.getImages().forEach((identifier, nativeImage) -> this.images.put(writer.add(identifier), writer.add(nativeImage)));
		this.sizes = font.getSizes();
		this.template = font.getTemplate();
	}


	public UnicodeTextureFont export(RegistryReader handler) {
		Map<Identifier, NativeImage> out = new HashMap<>(this.images.list().size());
		this.images.forEach((key, value) -> out.put(handler.get(key), handler.get(value)));
		UnicodeTextureFont font = UnsafeHelper.allocateInstance(UnicodeTextureFont.class);
		UnicodeTextureFontAccessor accessor = ((UnicodeTextureFontAccessor) font);
		accessor.setSizes(this.sizes);
		accessor.setImages(out);
		accessor.setTemplate(this.template);
		return font;
	}
}
