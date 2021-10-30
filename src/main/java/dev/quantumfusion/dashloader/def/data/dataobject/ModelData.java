package dev.quantumfusion.dashloader.def.data.dataobject;

import dev.quantumfusion.dashloader.core.registry.ChunkDataHolder;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.core.registry.chunk.data.AbstractDataChunk;
import dev.quantumfusion.dashloader.def.data.model.DashModel;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BakedModel;

import java.util.Collection;
import java.util.List;

@Data
public class ModelData implements ChunkDataHolder {
	public final AbstractDataChunk<BakedModel, DashModel> modelData;

	public ModelData(AbstractDataChunk<BakedModel, DashModel> modelData) {
		this.modelData = modelData;
	}

	public ModelData(DashRegistryWriter writer) {
		this.modelData = writer.getChunk(DashModel.class).exportData();
	}

	@Override
	public Collection<AbstractDataChunk<?, ?>> getChunks() {
		return List.of(modelData);
	}
}
