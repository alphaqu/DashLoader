package dev.quantumfusion.dashloader.def.corehook;

import dev.quantumfusion.dashloader.core.registry.ChunkDataHolder;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.core.registry.chunk.data.AbstractDataChunk;
import dev.quantumfusion.dashloader.def.data.image.DashImage;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.texture.NativeImage;

import java.util.Collection;
import java.util.List;

@Data
public class ImageData implements ChunkDataHolder {
	public final AbstractDataChunk<NativeImage, DashImage> imageData;

	public ImageData(AbstractDataChunk<NativeImage, DashImage> imageData) {
		this.imageData = imageData;
	}

	public ImageData(DashRegistryWriter writer) {
		this.imageData = writer.getChunk(DashImage.class).exportData();
	}

	@Override
	public Collection<AbstractDataChunk<?, ?>> getChunks() {
		return List.of(imageData);
	}
}
