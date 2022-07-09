package dev.quantumfusion.dashloader.data.registry;

import dev.quantumfusion.dashloader.data.model.DashModel;
import dev.quantumfusion.dashloader.registry.ChunkHolder;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.chunk.data.AbstractDataChunk;
import net.minecraft.client.render.model.BakedModel;

public class ModelData implements ChunkHolder {
	public final AbstractDataChunk<BakedModel, DashModel> modelData;

	public ModelData(AbstractDataChunk<BakedModel, DashModel> modelData) {
		this.modelData = modelData;
	}

	public ModelData(RegistryWriter writer) {
		this.modelData = writer.getChunk(DashModel.class).exportData();
	}

	@Override
	public AbstractDataChunk<?, ?>[] getChunks() {
		return new AbstractDataChunk[]{this.modelData};
	}
}
