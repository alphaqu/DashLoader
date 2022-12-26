package dev.quantumfusion.dashloader.registry;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/** The Writers job is to allow dashObject to add dependencies by adding them to the registry and allowing parallelization.
 * The logic is actually in RegistryFactory but we need to be able to track what added what so the writer gets issued on the invocation of the creator.
 */
public class RegistryWriter {
	private final RegistryFactory factory;
	private final IntList dependencies = new IntArrayList();

	public RegistryWriter(RegistryFactory factory) {
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

	public IntList getDependencies() {
		return dependencies;
	}
}
