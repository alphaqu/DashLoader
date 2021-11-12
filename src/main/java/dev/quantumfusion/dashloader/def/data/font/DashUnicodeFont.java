package dev.quantumfusion.dashloader.def.data.font;

import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.common.IntIntList;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.mixin.accessor.UnicodeTextureFontAccessor;
import dev.quantumfusion.dashloader.def.util.UnsafeHelper;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.font.UnicodeTextureFont;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
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
		images = new IntIntList(new ArrayList<>());
		UnicodeTextureFontAccessor font = ((UnicodeTextureFontAccessor) rawFont);
		font.getImages().forEach((identifier, nativeImage) -> images.put(writer.add(identifier), writer.add(nativeImage)));
		this.sizes = font.getSizes();
		this.template = font.getTemplate();
	}


	public UnicodeTextureFont export(RegistryReader handler) {
		Map<Identifier, NativeImage> out = new HashMap<>(images.list().size());
		images.forEach((key, value) -> out.put(handler.get(key), handler.get(value)));
		UnicodeTextureFont font = UnsafeHelper.allocateInstance(UnicodeTextureFont.class);
		UnicodeTextureFontAccessor accessor = ((UnicodeTextureFontAccessor) font);
		accessor.setSizes(sizes);
		accessor.setImages(out);
		accessor.setTemplate(template);
		return font;
	}
}
