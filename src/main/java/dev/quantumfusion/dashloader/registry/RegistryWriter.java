package dev.quantumfusion.dashloader.registry;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.data.ChunkFactory;
import dev.quantumfusion.dashloader.util.RegistryUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.function.Function;

/** The Writers job is to allow dashObject to add dependencies by adding them to the registry and allowing parallelization.
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

		// Increment dependency references
		int[] dependencies = writer.dependencies.toIntArray();
		for (int dependency : dependencies) {
			ChunkFactory<?, ?> chunk = factory.chunks[RegistryUtil.getChunkId(dependency)];
			ChunkFactory.Entry<?> entry = chunk.list.get(RegistryUtil.getObjectId(dependency));
			entry.references++;
		}

		return new ChunkFactory.Entry<>(data, dependencies);
	}

}
