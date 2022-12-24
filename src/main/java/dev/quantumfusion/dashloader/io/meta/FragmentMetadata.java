package dev.quantumfusion.dashloader.io.meta;

import com.github.luben.zstd.Zstd;
import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.util.IOHelper;
import dev.quantumfusion.hyphen.HyphenSerializer;
import dev.quantumfusion.hyphen.io.ByteBufferIO;
import dev.quantumfusion.taski.builtin.StepTask;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class FragmentMetadata {
	public final int rangeStart;
	public final int rangeEnd;
	public final int fileSize;

	public FragmentMetadata(int rangeStart, int rangeEnd, int fileSize) {
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
		this.fileSize = fileSize;
	}

	public void serialize(Dashable<?>[] dashables, HyphenSerializer<ByteBufferIO, ?> serializer, Path path, int compressionLevel, StepTask parent) throws IOException {
		try (FileChannel channel = IOHelper.createFile(path)) {
			if (compressionLevel > 0) {
				StepTask task = new StepTask("Serializing", 4);
				parent.setSubTask(task);

				// Allocate
				final var src = ByteBufferIO.createDirect(this.fileSize);
				DashLoader.LOG.info("Allocating {}MB", this.fileSize / 1024 / 1024);
				task.next();

				// Serialize
				this.put(dashables, serializer, src);
				task.next();

				// Compress
				src.rewind();
				final long maxSize = Zstd.compressBound(this.fileSize);
				final var dst = ByteBufferIO.createDirect((int) maxSize);
				DashLoader.LOG.info("Allocating {}MB", maxSize / 1024 / 1024);
				final long size = Zstd.compress(dst.byteBuffer, src.byteBuffer, compressionLevel);
				DashLoader.LOG.info("Compressed to {}MB", size / 1024 / 1024);
				src.close();
				task.next();

				// Write
				dst.rewind();
				dst.byteBuffer.limit((int) size);
				final var map = channel.map(FileChannel.MapMode.READ_WRITE, 0, size).order(ByteOrder.LITTLE_ENDIAN);
				map.put(dst.byteBuffer);
				map.clear();
				src.close();
				dst.close();
				task.next();
			} else {
				StepTask task = new StepTask("Serializing", 2);
				parent.setSubTask(task);
				final var map = channel.map(FileChannel.MapMode.READ_WRITE, 0, this.fileSize).order(ByteOrder.LITTLE_ENDIAN);
				ByteBufferIO file = ByteBufferIO.wrap(map);
				task.next();
				this.put(dashables, serializer, file);
				task.next();
			}
		}
	}

	public Runnable deserialize(Dashable<?>[] dashables, HyphenSerializer<ByteBufferIO, ?> serializer, Path path, int compressionLevel) throws IOException {
		try (FileChannel channel = IOHelper.openFile(path)) {
			var buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()).order(ByteOrder.LITTLE_ENDIAN);

			// Check compression
			return () -> {
				DashLoader.LOG.info("Fragment " + rangeStart + ".." + rangeEnd);
				if (compressionLevel > 0) {
					final var dst = ByteBufferIO.createDirect(this.fileSize);
					Zstd.decompress(dst.byteBuffer, buffer);
					dst.rewind();

					get(dashables, serializer, dst);
					dst.close();
				} else {
					get(dashables, serializer, ByteBufferIO.wrap(buffer));
				}
			};
		}
	}
	private void put(Dashable<?>[] dashables, HyphenSerializer<ByteBufferIO, ?> serializer, ByteBufferIO io) {
		for (int i = rangeStart; i < rangeEnd; i++) {
			((HyphenSerializer) serializer).put(io, dashables[i]);
		}
	}

	private void get(Dashable<?>[] dashables, HyphenSerializer<ByteBufferIO, ?> serializer, ByteBufferIO io) {
		for (int i = rangeStart; i < rangeEnd; i++) {
			dashables[i] = (Dashable<?>) ((HyphenSerializer) serializer).get(io);
		}
	}
}
