package dev.quantumfusion.dashloader.registry.chunk;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.RegistryReader;

import static dev.quantumfusion.dashloader.DashLoader.DL;

public class DataChunk<R, D extends Dashable<R>> {
	public final byte chunkId;
	public final String name;
	public final D[] dashables;

	public DataChunk(byte chunkId, String name, D[] dashables) {
		this.chunkId = chunkId;
		this.name = name;
		this.dashables = dashables;
	}

	public void preExport(RegistryReader reader) {
		D[] ds = this.dashables;
		for (int i = 0; i < ds.length; i++) {
			D dashable = ds[i];
			if (dashable == null) {
				System.out.println(name + " / " + i);
			}
			dashable.preExport(reader);
		}
	}

	public void export(Object[] data, RegistryReader registry) {
		DL.thread.parallelExport(this.dashables, data, registry);
	}

	public void postExport(RegistryReader reader) {
		for (D dashable : this.dashables) {
			dashable.postExport(reader);
		}
	}

	public int getSize() {
		return this.dashables.length;
	}
}
