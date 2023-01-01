package dev.notalpha.dashloader.io;

import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.io.serializer.UnsafeByteBufferDef;
import dev.notalpha.dashloader.registry.data.ChunkData;
import dev.notalpha.dashloader.util.IOHelper;
import dev.quantumfusion.hyphen.ClassDefiner;
import dev.quantumfusion.hyphen.HyphenSerializer;
import dev.quantumfusion.hyphen.SerializerFactory;
import dev.quantumfusion.hyphen.io.ByteBufferIO;
import dev.quantumfusion.hyphen.scan.annotations.DataSubclasses;
import dev.quantumfusion.taski.builtin.StepTask;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Serializer<O> {
	private final HyphenSerializer<ByteBufferIO, O> serializer;
	public Serializer(Path cacheDir, Class<O> aClass) {
		var name = aClass.getName();
		var serializerFileLocation = cacheDir.resolve(name.toLowerCase().replace(".", "_") + ".dlc");

		HyphenSerializer<ByteBufferIO, O> serializer = null;
		prepareFile(serializerFileLocation);
		if (Files.exists(serializerFileLocation)) {
			var classDefiner = new ClassDefiner(Thread.currentThread().getContextClassLoader());
			try {
				classDefiner.def(getSerializerClassName(aClass), Files.readAllBytes(serializerFileLocation));
				//noinspection unchecked
				serializer = (HyphenSerializer<ByteBufferIO, O>) ClassDefiner.SERIALIZER;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (serializer == null) {
			var factory = SerializerFactory.createDebug(ByteBufferIO.class, aClass);
			factory.addGlobalAnnotation(ChunkData.class, DataSubclasses.class, new Class[]{ChunkData.class});
			factory.setClassName(getSerializerClassName(aClass));
			factory.setExportPath(serializerFileLocation);
			factory.addDynamicDef(ByteBuffer.class, UnsafeByteBufferDef::new);
			serializer = factory.build();
		}

		this.serializer = serializer;
	}

	public O get(ByteBufferIO io) {
		return this.serializer.get(io);
	}

	public void put(ByteBufferIO io, O data) {
		this.serializer.put(io, data);
	}

	public long measure(O data) {
		return this.serializer.measure(data);
	}

	public void save(Path path, StepTask task, O data) {
		var measure = (int) this.serializer.measure(data);
		var io = ByteBufferIO.createDirect(measure);
		this.serializer.put(io, data);
		io.rewind();
		try {

			IOHelper.save(path, task, io, measure, ConfigHandler.INSTANCE.config.compression);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public O load(Path path) {
		try {
			ByteBufferIO io = IOHelper.load(path);
			return this.serializer.get(io);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	private static <O> String getSerializerClassName(Class<O> holderClass) {
		return holderClass.getSimpleName().toLowerCase() + "-serializer";
	}

	private static void prepareFile(Path path) {
		try {
			Files.createDirectories(path.getParent());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
