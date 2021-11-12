package dev.quantumfusion.dashloader.def.corehook;

import dev.quantumfusion.dashloader.core.registry.ChunkHolder;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.core.registry.chunk.data.AbstractDataChunk;
import dev.quantumfusion.dashloader.def.data.image.DashImage;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.texture.NativeImage;

@Data
public class ImageData implements ChunkHolder {
	public final AbstractDataChunk<NativeImage, DashImage> imageData;

	public ImageData(AbstractDataChunk<NativeImage, DashImage> imageData) {
		this.imageData = imageData;
	}

	public ImageData(RegistryWriter writer) {
		this.imageData = writer.getChunk(DashImage.class).exportData();
	}

	@Override
	public AbstractDataChunk<?, ?>[] getChunks() {
		return new AbstractDataChunk[]{imageData};
	}
}
