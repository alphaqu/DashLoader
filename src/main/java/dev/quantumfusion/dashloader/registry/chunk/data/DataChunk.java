package dev.quantumfusion.dashloader.registry.chunk.data;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.RegistryReader;

import static dev.quantumfusion.dashloader.DashLoader.DL;

public class DataChunk<R, D extends Dashable<R>> extends AbstractDataChunk<R, D> {
	public final D[] dashables;

	public DataChunk(byte pos, String name, D[] dashables) {
		super(pos, name);
		this.dashables = dashables;
	}

	@Override
	public void preExport(RegistryReader reader) {
		for (D dashable : this.dashables) {
			dashable.preExport(reader);
		}
	}

	@Override
	public void export(Object[] data, RegistryReader registry) {
		DL.thread.parallelExport(this.dashables, data, registry);
	}

	@Override
	public void postExport(RegistryReader reader) {
		for (D dashable : this.dashables) {
			dashable.postExport(reader);
		}
	}

	@Override
	public int getDashableSize() {
		return this.dashables.length;
	}
}
