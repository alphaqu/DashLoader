package dev.quantumfusion.dashloader.data.registry;

import dev.quantumfusion.dashloader.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.registry.ChunkHolder;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.chunk.data.DataChunk;
import net.minecraft.util.Identifier;

public class IdentifierData implements ChunkHolder {
	public final DataChunk<Identifier, DashIdentifierInterface> identifierData;

	public IdentifierData(DataChunk<Identifier, DashIdentifierInterface> identifierData) {
		this.identifierData = identifierData;
	}

	public IdentifierData(RegistryWriter writer) {
		this.identifierData = writer.getChunk(DashIdentifierInterface.class).exportData();
	}

	@Override
	public DataChunk<?, ?>[] getChunks() {
		return new DataChunk[]{this.identifierData};
	}
}
