package dev.quantumfusion.dashloader.io;

import dev.quantumfusion.dashloader.io.meta.CacheMetadata;
import dev.quantumfusion.dashloader.util.IOHelper;
import dev.quantumfusion.hyphen.HyphenSerializer;
import dev.quantumfusion.hyphen.SerializerFactory;
import dev.quantumfusion.hyphen.io.ByteBufferIO;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class MetadataSerializer {
	private final HyphenSerializer<ByteBufferIO, CacheMetadata> serializer;

	public MetadataSerializer() {
		this.serializer = SerializerFactory.createDebug(ByteBufferIO.class, CacheMetadata.class).build();
	}

	public void serialize(Path dir,CacheMetadata metadata) throws IOException {
		try (FileChannel file = IOHelper.createFile(metadataFilePath(dir))) {
			long measure = serializer.measure(metadata);
			final var map = file.map(FileChannel.MapMode.READ_WRITE, 0, measure).order(ByteOrder.LITTLE_ENDIAN);
			ByteBufferIO io = ByteBufferIO.wrap(map);
			serializer.put(io, metadata);
		}
	}

	public CacheMetadata deserialize(Path dir) throws IOException {
		try (FileChannel file = IOHelper.openFile(metadataFilePath(dir))) {
			final var map = file.map(FileChannel.MapMode.READ_ONLY, 0, file.size()).order(ByteOrder.LITTLE_ENDIAN);
			ByteBufferIO io = ByteBufferIO.wrap(map);
			return serializer.get(io);
		}
	}


	private Path metadataFilePath(Path dir) {
		return dir.resolve("metadata.bin");
	}
}
