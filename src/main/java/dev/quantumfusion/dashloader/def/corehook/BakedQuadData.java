package dev.quantumfusion.dashloader.def.corehook;

import dev.quantumfusion.dashloader.core.registry.ChunkDataHolder;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.core.registry.chunk.data.AbstractDataChunk;
import dev.quantumfusion.dashloader.def.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.def.data.model.components.DashBakedQuad;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;

@Data
public class BakedQuadData implements ChunkDataHolder {
	public final AbstractDataChunk<BakedQuad, DashBakedQuad> bakedQuadData;

	public BakedQuadData(AbstractDataChunk<BakedQuad, DashBakedQuad> bakedQuadData) {
		this.bakedQuadData = bakedQuadData;
	}

	public BakedQuadData(DashRegistryWriter writer) {
		this.bakedQuadData = writer.getChunk(DashBakedQuad.class).exportData();
	}

	@Override
	public Collection<AbstractDataChunk<?, ?>> getChunks() {
		return List.of(bakedQuadData);
	}
}
