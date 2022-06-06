package dev.quantumfusion.dashloader.registry.chunk.data;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.thread.IndexedArrayMapTask;
import dev.quantumfusion.dashloader.thread.ThreadHandler;
import static dev.quantumfusion.dashloader.DashLoader.DL;

public class StagedDataChunk<R, D extends Dashable<R>> extends AbstractDataChunk<R, D> {
	public final IndexedArrayMapTask.IndexedArrayEntry<D>[][] dashables;

	public final int dashablesSize;

	public StagedDataChunk(byte pos, String name, IndexedArrayMapTask.IndexedArrayEntry<D>[][] dashables, int dashablesSize) {
		super(pos, name);
		this.dashables = dashables;
		this.dashablesSize = dashablesSize;
	}

	@Override
	public void preExport(RegistryReader reader) {
		for (var stage : this.dashables) {
			for (var entry : stage) {
				entry.object().preExport(reader);
			}
		}

	}

	@Override
	public void export(Object[] data, RegistryReader registry) {
		final ThreadHandler threadHandler = DL.thread;
		for (IndexedArrayMapTask.IndexedArrayEntry<D>[] dashable : this.dashables) {
			threadHandler.parallelExport(dashable, data, registry);
		}
	}

	@Override
	public void postExport(RegistryReader reader) {
		for (var stage : this.dashables) {
			for (var entry : stage) {
				entry.object().postExport(reader);
			}
		}

	}

	@Override
	public int getDashableSize() {
		return this.dashablesSize;
	}
}
