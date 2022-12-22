package dev.quantumfusion.dashloader.registry.chunk.write;

import dev.quantumfusion.dashloader.DashObjectClass;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.chunk.data.DataChunk;
import dev.quantumfusion.dashloader.registry.chunk.data.SimpleDataChunk;
import dev.quantumfusion.dashloader.registry.factory.DashFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WriteChunk<R, D extends Dashable<R>> extends AbstractWriteChunk<R, D> {
	private final List<D> list = new ArrayList<>();

	public WriteChunk(byte pos, String name, RegistryWriter writer, Collection<DashObjectClass<R, D>> dashObjects, DashFactory<R, D> factory) {
		super(pos, name, writer, factory, dashObjects);
	}

	@Override
	public int add(R raw) {
		final int pos = this.list.size();
		this.list.add(this.factory.create(raw, this.writer));
		return pos;
	}

	@Override
	public DataChunk<R, D> exportData() {
		//noinspection unchecked
		final D[] dashables = (D[]) this.list.toArray(Dashable[]::new);
		return new SimpleDataChunk<>(this.pos, this.name, dashables);
	}
}
