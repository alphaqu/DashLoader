package dev.quantumfusion.dashloader.registry.chunk.data;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.util.DashUtil;

public class SimpleDataFragment<R, D extends Dashable<R>> {
	public final D[] dashables;

	public SimpleDataFragment(D[] dashables) {
		this.dashables = dashables;
	}

	public static <R, D extends Dashable<R>> SimpleDataFragment<R, D>[] split(SimpleDataChunk<R, D> data, int fragments) {
		D[][] ds = DashUtil.fragmentArray(data.dashables, fragments);
		SimpleDataFragment<R, D>[] out = new SimpleDataFragment[fragments];
		for (int i = 0; i < ds.length; i++) {
			out[i] = new SimpleDataFragment<>(ds[i]);
		}
		return out;
	}


	public static <R, D extends Dashable<R>> SimpleDataChunk<R, D> combine(SimpleDataFragment<R, D>[] fragments, DataChunkHeader header) {
		D[][] frags = (D[][]) new Object[fragments.length][];
		for (int i = 0; i < fragments.length; i++) {
			frags[i] = fragments[i].dashables;
		}
		D[] ds = DashUtil.combineArray(frags);

		return new SimpleDataChunk<>(header.pos, header.name, ds);
	}
}
