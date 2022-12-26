package dev.quantumfusion.dashloader.registry.data;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.api.DashObjectClass;
import dev.quantumfusion.dashloader.io.fragment.Piece;
import dev.quantumfusion.dashloader.registry.RegistryReader;

import java.util.Arrays;
import java.util.List;

import static dev.quantumfusion.dashloader.DashLoader.DL;

public class ChunkData<R, D extends Dashable<R>> {
	public final byte chunkId;
	public final String name;
	public final DashObjectClass<?, ?> dashObject;
	public final Entry<D>[] dashables;

	public ChunkData(byte chunkId, String name, DashObjectClass<?, ?> dashObject, Entry<D>[] dashables) {
		this.chunkId = chunkId;
		this.name = name;
		this.dashObject = dashObject;
		this.dashables = dashables;
	}

	public void preExport(RegistryReader reader) {
		Entry<D>[] ds = this.dashables;
		for (int i = 0; i < ds.length; i++) {
			Entry<D> dashable = ds[i];
			if (dashable == null) {
				System.out.println(name + " / " + i);
			}
			dashable.data.preExport(reader);
		}
	}

	public void export(Object[] data, RegistryReader registry) {
		DL.thread.parallelExport(this.dashables, data, registry);
	}

	public void postExport(RegistryReader reader) {
		for (Entry<D> dashable : this.dashables) {
			dashable.data.postExport(reader);
		}
	}

	public int getSize() {
		return this.dashables.length;
	}

	public static final class Entry<D> {
		public final D data;
		public final int pos;

		public Entry(D data, int pos) {
			this.data = data;
			this.pos = pos;
		}
	}
}
