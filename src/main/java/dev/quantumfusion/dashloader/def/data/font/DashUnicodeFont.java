package dev.quantumfusion.dashloader.def.data.font;

import dev.quantumfusion.dashloader.def.mixin.accessor.UnicodeTextureFontAccessor;
import dev.quantumfusion.dashloader.def.util.UnsafeHelper;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.font.UnicodeTextureFont;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.core.data.IntIntList;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.annotations.DashObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
@DashObject(value = UnicodeTextureFont.class)
public class DashUnicodeFont implements DashFont {
	public final IntIntList images;
	public final byte[] sizes;
	public final String template;

	public DashUnicodeFont(IntIntList images, byte[] sizes, String template) {
		this.images = images;
		this.sizes = sizes;
		this.template = template;
	}

	public DashUnicodeFont(UnicodeTextureFont rawFont, DashRegistry registry) {
		images = new IntIntList(new ArrayList<>());
		UnicodeTextureFontAccessor font = ((UnicodeTextureFontAccessor) rawFont);
		font.getImages().forEach((identifier, nativeImage) -> images.put(registry.add(identifier), registry.add(nativeImage)));
		this.sizes = font.getSizes();
		this.template = font.getTemplate();
	}


	public UnicodeTextureFont toUndash(DashExportHandler handler) {
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
