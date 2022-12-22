package dev.quantumfusion.dashloader.data.registry;

import dev.quantumfusion.dashloader.data.image.DashImage;
import dev.quantumfusion.dashloader.registry.ChunkHolder;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.chunk.data.DataChunk;
import net.minecraft.client.texture.NativeImage;

public class ImageData implements ChunkHolder {
	public final DataChunk<NativeImage, DashImage> imageData;

	public ImageData(DataChunk<NativeImage, DashImage> imageData) {
		this.imageData = imageData;
	}

	public ImageData(RegistryWriter writer) {
		this.imageData = writer.getChunk(DashImage.class).exportData();
	}

	@Override
	public DataChunk<?, ?>[] getChunks() {
		return new DataChunk[]{this.imageData};
	}
}
