package dev.quantumfusion.dashloader.io.serializer;

import com.github.luben.zstd.Zstd;
import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.api.DashObjectClass;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.data.ChunkData;
import dev.quantumfusion.hyphen.ClassDefiner;
import dev.quantumfusion.hyphen.HyphenSerializer;
import dev.quantumfusion.hyphen.SerializerFactory;
import dev.quantumfusion.hyphen.io.ByteBufferIO;
import dev.quantumfusion.hyphen.scan.annotations.DataSubclasses;
import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StepTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.quantumfusion.dashloader.DashLoader.DL;

public class SimpleSerializer<O> implements DataSerializer<O> {

	private static final int HEADER_SIZE = 5;
	private final String name;
	private final HyphenSerializer<ByteBufferIO, O> serializer;
	private final byte compressionLevel;

	public SimpleSerializer(String name, HyphenSerializer<ByteBufferIO, O> serializer) {
		this.name = name;
		this.serializer = serializer;
		this.compressionLevel = DL.config.config.compression;
	}

	public static <F> SimpleSerializer<F> create(String name, Path cacheArea, Class<F> holderClass, List<DashObjectClass<?, ?>> dashObjects, Class<? extends Dashable<?>>[] dashables) {
		var serializerFileLocation = cacheArea.resolve(holderClass.getSimpleName().toLowerCase() + ".dlc");
		prepareFile(serializerFileLocation);
		if (Files.exists(serializerFileLocation)) {
			var classDefiner = new ClassDefiner(Thread.currentThread().getContextClassLoader());
			try {
				classDefiner.def(getSerializerName(holderClass), Files.readAllBytes(serializerFileLocation));
				//noinspection unchecked
				return new SimpleSerializer<>(name, (HyphenSerializer<ByteBufferIO, F>) ClassDefiner.SERIALIZER);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		var factory = SerializerFactory.createDebug(ByteBufferIO.class, holderClass);
		factory.addGlobalAnnotation(ChunkData.class, DataSubclasses.class, new Class[]{ChunkData.class});
		factory.setClassName(getSerializerName(holderClass));
		factory.setExportPath(serializerFileLocation);
		factory.addDynamicDef(ByteBuffer.class, UnsafeByteBufferDef::new);
		for (Class<? extends Dashable> dashable : dashables) {
			var dashClasses = new ArrayList<Class<?>>();
		//for (var dashObject : dashObjects) {
		//	if (dashable == dashObject.getTag()) {
		//		dashClasses.add(dashObject.getDashClass());
		//	}
		//}

			dashClasses.remove(dashable);
			if (dashClasses.size() > 0) {
				factory.addGlobalAnnotation(dashable, DataSubclasses.class, dashClasses.toArray(Class[]::new));
			}
		}
		return new SimpleSerializer<>(name, factory.build());
	}

	@NotNull
	private static <O> String getSerializerName(Class<O> holderClass) {
		return holderClass.getSimpleName().toLowerCase() + "-serializer";
	}

	private static void prepareFile(Path path) {
		try {
			Files.createDirectories(path.getParent());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@NotNull
	private Path getFilePath(Path subCache) {
		return subCache.resolve(this.name + ".dld");
	}

	@Override
	public void encode(O object, Path subCache, @Nullable Consumer<Task> taskConsumer) throws IOException {
		StepTask task = new StepTask(this.name, this.compressionLevel > 0 ? 5 : 2);
		if (taskConsumer != null) {
			taskConsumer.accept(task);
		}

		final Path outPath = this.getFilePath(subCache);
		prepareFile(outPath);

		try (FileChannel channel = FileChannel.open(outPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
			final long rawFileSize = this.serializer.measure(object);
			if (rawFileSize > Integer.MAX_VALUE) {
				throw new IOException("Size of data is " + rawFileSize + " which is bigger than 2GB");
			}

			if (this.compressionLevel > 0) {
				// Allocate
				final long maxSize = Zstd.compressBound(rawFileSize);
				final var dst = ByteBufferIO.createDirect((int) maxSize);
				final var src = ByteBufferIO.createDirect((int) rawFileSize);
				task.next();

				// Serialize
				this.serializer.put(src, object);

				task.next();

				// Compress
				src.rewind();
				final long size = Zstd.compress(dst.byteBuffer, src.byteBuffer, this.compressionLevel);
				task.next();

				// Write
				dst.rewind();
				dst.byteBuffer.limit((int) size);
				final var map = channel.map(FileChannel.MapMode.READ_WRITE, 0, size + HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
				task.next();

				map.put(this.compressionLevel);
				map.putInt((int) rawFileSize);
				map.put(dst.byteBuffer);
				src.close();
				dst.close();
			} else {
				final var map = channel.map(FileChannel.MapMode.READ_WRITE, 0, rawFileSize + 1).order(ByteOrder.LITTLE_ENDIAN);
				task.next();
				ByteBufferIO file = ByteBufferIO.wrap(map);
				file.putByte(this.compressionLevel);
				this.serializer.put(file, object);
			}
		}
		task.next();
	}

	@Override
	public O decode(Path subCache) throws IOException {
		long start = System.currentTimeMillis();
		try (FileChannel channel = FileChannel.open(this.getFilePath(subCache))) {
			var buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()).order(ByteOrder.LITTLE_ENDIAN);
			DashLoader.LOG.info("Read {} in {}ms", this.name, System.currentTimeMillis() - start);
			start = System.currentTimeMillis();

			// Check compression
			if (buffer.get() > 0) {
				final int size = buffer.getInt();
				final var dst = ByteBufferIO.createDirect(size);
				Zstd.decompress(dst.byteBuffer, buffer);
				dst.rewind();
				O object = this.serializer.get(dst);
				dst.close();
				DashLoader.LOG.info("Decompressed {} in {}ms", this.name, System.currentTimeMillis() - start);
				return object;
			} else {
				return this.serializer.get(ByteBufferIO.wrap(buffer));
			}
		}
	}
}
