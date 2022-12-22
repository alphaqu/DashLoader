package dev.quantumfusion.dashloader.data.registry;

import dev.quantumfusion.dashloader.data.model.components.DashBakedQuad;
import dev.quantumfusion.dashloader.registry.ChunkHolder;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.chunk.data.DataChunk;
import net.minecraft.client.render.model.BakedQuad;

public class BakedQuadData implements ChunkHolder {
	public final DataChunk<BakedQuad, DashBakedQuad> bakedQuadData;

	public BakedQuadData(DataChunk<BakedQuad, DashBakedQuad> bakedQuadData) {
		this.bakedQuadData = bakedQuadData;
	}

	public BakedQuadData(RegistryWriter writer) {
		this.bakedQuadData = writer.getChunk(DashBakedQuad.class).exportData();
	}

	@Override
	public DataChunk<?, ?>[] getChunks() {
		return new DataChunk[]{this.bakedQuadData};
	}
}
