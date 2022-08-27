package dev.quantumfusion.dashloader.registry.chunk.write;

import dev.quantumfusion.dashloader.DashObjectClass;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.chunk.data.AbstractDataChunk;
import dev.quantumfusion.dashloader.registry.chunk.data.DataChunk;
import dev.quantumfusion.dashloader.registry.factory.DashFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FloatingWriteChunk<R, D extends Dashable<R>> extends AbstractWriteChunk<R, D> {
	private final List<R> list = new ArrayList<>();

	public FloatingWriteChunk(byte pos, String name, RegistryWriter writer, Collection<DashObjectClass<R, D>> dashObjects, DashFactory<R, D> factory) {
		super(pos, name, writer, factory, dashObjects);
	}

	@Override
	public int add(R raw) {
		final int pos = this.list.size();
		this.list.add(raw);
		return pos;
	}

	@Override
	public AbstractDataChunk<R, D> exportData() {
		final int length = this.list.size();
		//noinspection unchecked
		final D[] dashables = (D[]) new Dashable[length];
		for (int i = 0; i < length; i++) {
			dashables[i] = this.factory.create(this.list.get(i), this.writer);
		}
		return new DataChunk<>(this.pos, this.name, dashables);
	}
}
