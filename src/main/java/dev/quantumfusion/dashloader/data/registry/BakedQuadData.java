package dev.quantumfusion.dashloader.data.registry;

import dev.quantumfusion.dashloader.data.model.components.DashBakedQuad;
import dev.quantumfusion.dashloader.registry.ChunkHolder;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.chunk.data.AbstractDataChunk;
import net.minecraft.client.render.model.BakedQuad;

public class BakedQuadData implements ChunkHolder {
	public final AbstractDataChunk<BakedQuad, DashBakedQuad> bakedQuadData;

	public BakedQuadData(AbstractDataChunk<BakedQuad, DashBakedQuad> bakedQuadData) {
		this.bakedQuadData = bakedQuadData;
	}

	public BakedQuadData(RegistryWriter writer) {
		this.bakedQuadData = writer.getChunk(DashBakedQuad.class).exportData();
	}

	@Override
	public AbstractDataChunk<?, ?>[] getChunks() {
		return new AbstractDataChunk[]{this.bakedQuadData};
	}
}
