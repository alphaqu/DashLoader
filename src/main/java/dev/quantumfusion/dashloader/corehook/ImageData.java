package dev.quantumfusion.dashloader.corehook;

import dev.quantumfusion.dashloader.data.image.DashImage;
import dev.quantumfusion.dashloader.registry.ChunkHolder;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.chunk.data.AbstractDataChunk;
import net.minecraft.client.texture.NativeImage;

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
		return new AbstractDataChunk[]{this.imageData};
	}
}
