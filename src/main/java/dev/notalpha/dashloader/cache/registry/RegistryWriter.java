package dev.notalpha.dashloader.cache.registry;

import dev.notalpha.dashloader.api.Dashable;
import dev.notalpha.dashloader.cache.registry.data.ChunkFactory;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.function.Function;

/**
 * The Writers job is to allow dashObject to add dependencies by adding them to the registry and allowing parallelization.
 * The logic is actually in RegistryFactory but we need to be able to track what added what so the writer gets issued on the invocation of the creator.
 */
public class RegistryWriter {
	private final RegistryFactory factory;
	private final IntList dependencies = new IntArrayList();

	private RegistryWriter(RegistryFactory factory) {
		this.factory = factory;
	}

	/**
	 * @see RegistryWriter#add(Object)
	 */
	public <R> int add(R object) {
		int value = factory.add(object);
		dependencies.add(value);
		return value;
	}

	static <R, D extends Dashable<R>> ChunkFactory.Entry<D> create(RegistryFactory factory, Function<RegistryWriter, D> function) {
		RegistryWriter writer = new RegistryWriter(factory);
		D data = function.apply(writer);
		int[] dependencies = writer.dependencies.toIntArray();
		return new ChunkFactory.Entry<>(data, dependencies);
	}

}
