package dev.notalpha.dashloader.registry;

import dev.notalpha.dashloader.api.RegistryWriter;
import dev.notalpha.dashloader.registry.data.ChunkFactory;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.function.Function;

/**
 * The Writers job is to allow dashObject to add dependencies by adding them to the registry and allowing parallelization.
 * The logic is actually in RegistryFactory but we need to be able to track what added what so the writer gets issued on the invocation of the creator.
 */
class TrackedRegistryFactory implements RegistryWriter {
	private final RegistryFactory factory;
	private final IntList dependencies = new IntArrayList();

	private TrackedRegistryFactory(RegistryFactory factory) {
		this.factory = factory;
	}

	/**
	 * @see TrackedRegistryFactory#add(Object)
	 */
	public <R> int add(R object) {
		int value = factory.add(object);
		dependencies.add(value);
		return value;
	}

	static <D> ChunkFactory.Entry<D> create(RegistryFactory factory, Function<TrackedRegistryFactory, D> function) {
		TrackedRegistryFactory writer = new TrackedRegistryFactory(factory);
		D data = function.apply(writer);
		int[] dependencies = writer.dependencies.toIntArray();
		return new ChunkFactory.Entry<>(data, dependencies);
	}

}
