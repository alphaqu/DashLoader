package dev.notalpha.dashloader.registry.data;


import dev.notalpha.dashloader.registry.RegistryReaderImpl;

public class StageData {
	public final ChunkData<?, ?>[] chunks;

	public StageData(ChunkData<?, ?>[] chunks) {
		this.chunks = chunks;
	}

	public void preExport(RegistryReaderImpl reader) {
		for (ChunkData<?, ?> chunk : chunks) {
			chunk.preExport(reader);

		}
	}

	public void export(Object[][] data, RegistryReaderImpl registry) {
		for (int i = 0; i < chunks.length; i++) {
			ChunkData<?, ?> chunk = chunks[i];
			chunk.export(data[i], registry);
		}
	}

	public void postExport(RegistryReaderImpl reader) {
		for (ChunkData<?, ?> chunk : chunks) {
			chunk.postExport(reader);
		}
	}
}
