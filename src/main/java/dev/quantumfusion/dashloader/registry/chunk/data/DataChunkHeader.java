package dev.quantumfusion.dashloader.registry.chunk.data;

public class DataChunkHeader {
	public final byte pos;
	public final String name;

	public DataChunkHeader(DataChunk<?, ?> chunk) {
		this.pos = chunk.pos;
		this.name = chunk.name;
	}
}
