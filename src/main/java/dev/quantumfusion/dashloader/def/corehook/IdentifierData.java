package dev.quantumfusion.dashloader.def.corehook;

import dev.quantumfusion.dashloader.core.registry.ChunkHolder;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.core.registry.chunk.data.AbstractDataChunk;
import dev.quantumfusion.dashloader.def.data.DashIdentifierInterface;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;

@Data
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
		return new AbstractDataChunk[]{identifierData};
	}
}
