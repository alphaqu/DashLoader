package dev.quantumfusion.dashloader.data.registry;

import dev.quantumfusion.dashloader.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.registry.ChunkHolder;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.chunk.data.AbstractDataChunk;
import net.minecraft.util.Identifier;

public class IdentifierData implements ChunkHolder {
	public final AbstractDataChunk<Identifier, DashIdentifierInterface> identifierData;

	public IdentifierData(AbstractDataChunk<Identifier, DashIdentifierInterface> identifierData) {
		this.identifierData = identifierData;
	}

	public IdentifierData(RegistryWriter writer) {
		this.identifierData = writer.getChunk(DashIdentifierInterface.class).exportData();
	}

	@Override
	public AbstractDataChunk<?, ?>[] getChunks() {
		return new AbstractDataChunk[]{this.identifierData};
	}
}
