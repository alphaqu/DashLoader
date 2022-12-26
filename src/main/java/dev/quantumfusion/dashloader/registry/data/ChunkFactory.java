package dev.quantumfusion.dashloader.registry.data;

import dev.quantumfusion.dashloader.api.DashObjectClass;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.RegistryFactory;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.factory.DashFactory;
import dev.quantumfusion.dashloader.util.RegistryUtil;

import java.util.ArrayList;
import java.util.List;

public class ChunkFactory<R, D extends Dashable<R>> {
	public final byte chunkId;
	public final String name;
	public final DashObjectClass<R, D> dashObject;
	public final List<Entry<D>> list = new ArrayList<>();
	private final DashFactory<R, D> factory;

	public ChunkFactory(byte chunkId, String name, DashFactory<R, D> factory, DashObjectClass<R, D> dashObject) {
		this.chunkId = chunkId;
		this.name = name;
		this.factory = factory;
		this.dashObject = dashObject;
	}

	public int add(R raw, RegistryFactory factory) {
		RegistryWriter writer = new RegistryWriter(factory);
		D value = this.factory.create(raw, writer);

		// Increment dependency references
		int[] dependencies = writer.getDependencies().toIntArray();
		for (int dependency : dependencies) {
			ChunkFactory<?, ?> chunk = factory.chunks[RegistryUtil.getChunkId(dependency)];
			Entry<?> entry = chunk.list.get(RegistryUtil.getObjectId(dependency));
			entry.references++;
		}

		final int pos = this.list.size();
		this.list.add(new Entry<>(value, dependencies));
		return pos;
	}

	public List<Entry<D>> getList() {
		return list;
	}

	public static final class Entry<D> {
		public final D data;
		public final int[] dependencies;
		public int references = 0;
		public int stage = -1;

		public Entry(D data, int[] dependencies) {
			this.data = data;
			this.dependencies = dependencies;
		}
	}
}
