package dev.quantumfusion.dashloader.def.corehook;

import dev.quantumfusion.dashloader.core.registry.ChunkHolder;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.core.registry.chunk.data.AbstractDataChunk;
import dev.quantumfusion.dashloader.def.data.model.DashModel;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BakedModel;

import java.util.Collection;
import java.util.List;

@Data
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
		return new AbstractDataChunk[]{modelData};
	}
}
