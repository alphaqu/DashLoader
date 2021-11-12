package dev.quantumfusion.dashloader.def.corehook;

import dev.quantumfusion.dashloader.core.registry.ChunkHolder;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.core.registry.chunk.data.AbstractDataChunk;
import dev.quantumfusion.dashloader.def.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.def.data.model.components.DashBakedQuad;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;

@Data
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
		return new AbstractDataChunk[]{bakedQuadData};
	}
}
