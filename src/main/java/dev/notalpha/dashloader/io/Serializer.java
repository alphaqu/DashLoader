package dev.notalpha.dashloader.io;

import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.io.def.NativeImageData;
import dev.notalpha.dashloader.io.def.NativeImageDataDef;
import dev.notalpha.dashloader.registry.data.ChunkData;
import dev.notalpha.taski.builtin.StepTask;
import dev.notalpha.hyphen.HyphenSerializer;
import dev.notalpha.hyphen.SerializerFactory;
import dev.notalpha.hyphen.io.ByteBufferIO;
import dev.notalpha.hyphen.io.UnsafeIO;
import dev.notalpha.hyphen.scan.annotations.DataSubclasses;
import net.minecraft.client.font.UnihexFont;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;

public class Serializer<O> {
	private final HyphenSerializer<UnsafeIO, O> serializer;

	public Serializer(Class<O> aClass) {
		var factory = SerializerFactory.createDebug(UnsafeIO.class, aClass);
		factory.addAnnotationProvider(ChunkData.class, new DataSubclasses() {
			@Override
			public Class<?>[] value() {
				return  new Class[]{ChunkData.class};
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return DataSubclasses.class;
			}
		});
		factory.setClassName(getSerializerClassName(aClass));
		factory.addAnnotationProvider(UnihexFont.BitmapGlyph.class, new DataSubclasses() {
			@Override
			public Class<?>[] value() {
				return  new Class[]{
						UnihexFont.FontImage32x16.class,
						UnihexFont.FontImage16x16.class,
						UnihexFont.FontImage8x16.class,
				};
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return DataSubclasses.class;
			}
		});
		factory.addDynamicDef(NativeImageData.class, NativeImageDataDef::new);
		this.serializer = factory.build();
	}

	public O get(UnsafeIO io) {
		return this.serializer.get(io);
	}

	public void put(UnsafeIO io, O data) {
		this.serializer.put(io, data);
	}

	public long measure(O data) {
		return this.serializer.measure(data);
	}

	public void save(Path path, StepTask task, O data) {
		var measure = (int) this.serializer.measure(data);
		var io = UnsafeIO.create(measure);
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
			UnsafeIO io = IOHelper.load(path);
			O o = this.serializer.get(io);
			io.close();
			return o;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	private static <O> String getSerializerClassName(Class<O> holderClass) {
		return holderClass.getSimpleName().toLowerCase() + "-serializer";
	}
}
