package dev.quantumfusion.dashloader.registry.chunk.data;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.thread.IndexedArrayMapTask;
import dev.quantumfusion.dashloader.thread.ThreadHandler;

import static dev.quantumfusion.dashloader.DashLoader.DL;

public class StagedDataChunk<R, D extends Dashable<R>> extends DataChunk<R, D> {
	public final IndexedArrayMapTask.Entry<D>[][] stages;

	public final int size;

	public StagedDataChunk(byte pos, String name, IndexedArrayMapTask.Entry<D>[][] stages, int size) {
		super(pos, name);
		this.stages = stages;
		this.size = size;
	}

	@Override
	public void preExport(RegistryReader reader) {
		for (var stage : this.stages) {
			for (var entry : stage) {
				entry.object().preExport(reader);
			}
		}

	}

	@Override
	public void export(Object[] data, RegistryReader registry) {
		final ThreadHandler threadHandler = DL.thread;
		for (IndexedArrayMapTask.Entry<D>[] dashable : this.stages) {
			threadHandler.parallelExport(dashable, data, registry);
		}
	}

	@Override
	public void postExport(RegistryReader reader) {
		for (var stage : this.stages) {
			for (var entry : stage) {
				entry.object().postExport(reader);
			}
		}

	}

	@Override
	public int getSize() {
		return this.size;
	}
}
