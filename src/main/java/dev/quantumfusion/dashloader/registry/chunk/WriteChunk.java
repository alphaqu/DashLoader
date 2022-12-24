package dev.quantumfusion.dashloader.registry.chunk;

import dev.quantumfusion.dashloader.api.DashObjectClass;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.factory.DashFactory;

import java.util.ArrayList;
import java.util.List;

public class WriteChunk<R, D extends Dashable<R>> {
	public final byte chunkId;
	public final String name;
	public final DashObjectClass<R, D> dashObject;

	private final List<D> list = new ArrayList<>();
	private final RegistryWriter writer;
	private final DashFactory<R, D> factory;

	public WriteChunk(byte chunkId, String name, RegistryWriter writer, DashFactory<R, D> factory, DashObjectClass<R, D> dashObject) {
		this.chunkId = chunkId;
		this.name = name;
		this.writer = writer;
		this.factory = factory;
		this.dashObject = dashObject;
	}

	public int add(R raw) {
		D value = this.factory.create(raw, this.writer);
		final int pos = this.list.size();
		this.list.add(value);
		return pos;
	}

	public List<Class<?>> getTargetClasses() {
		return List.of(this.dashObject.getTargetClass());
	}

	public DataChunk<R, D> exportData() {
		//noinspection unchecked
		final D[] dashables = (D[]) this.list.toArray(Dashable[]::new);
		return new DataChunk<>(this.chunkId, this.name, dashables);
	}
}
