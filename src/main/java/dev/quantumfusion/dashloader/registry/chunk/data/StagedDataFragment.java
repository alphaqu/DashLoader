package dev.quantumfusion.dashloader.registry.chunk.data;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.thread.IndexedArrayMapTask;
import dev.quantumfusion.dashloader.util.DashUtil;

public class StagedDataFragment<R, D extends Dashable<R>> {
	public final IndexedArrayMapTask.Entry<D>[][] stages;

	public StagedDataFragment(IndexedArrayMapTask.Entry<D>[][] stages) {
		this.stages = stages;
	}

	public static <R, D extends Dashable<R>> StagedDataFragment<R, D>[] split(StagedDataChunk<R, D> data, int fragments) {
		IndexedArrayMapTask.Entry<D>[][][] stagesSplit = (IndexedArrayMapTask.Entry<D>[][][]) new IndexedArrayMapTask.Entry[fragments][][];


		for (int i = 0; i < data.stages.length; i++) {
			IndexedArrayMapTask.Entry<D>[][] indexedArrayEntries = DashUtil.fragmentArray(data.stages[i], fragments);
			stagesSplit[i] = indexedArrayEntries;
		}

		StagedDataFragment<R, D>[] out = new StagedDataFragment[fragments];
		for (int i = 0; i < fragments; i++) {
			IndexedArrayMapTask.Entry<D>[][] stages = (IndexedArrayMapTask.Entry<D>[][]) new IndexedArrayMapTask.Entry[data.stages.length][];
			for (int j = 0; j < data.stages.length; j++) {
				stages[j] = stagesSplit[j][i];
			}

			out[i] = new StagedDataFragment<>(stages);
		}
		return out;
	}


	public static <R, D extends Dashable<R>> StagedDataChunk<R, D> combine(StagedDataFragment<R, D>[] fragments, DataChunkHeader header) {
		int[] stageSizes = new int[fragments[0].stages.length];
		for (StagedDataFragment<R, D> fragment : fragments) {
			for (int i = 0; i < fragment.stages.length; i++) {
				stageSizes[i] += fragment.stages[i].length;
			}
		}

		IndexedArrayMapTask.Entry<D>[][] stages = new IndexedArrayMapTask.Entry[stageSizes.length][];
		int size = 0;
		for (int i = 0; i < stages.length; i++) {
			int stageSize = stageSizes[i];
			IndexedArrayMapTask.Entry<D>[] stage = new IndexedArrayMapTask.Entry[stageSize];
			int stagePos = 0;
			for (StagedDataFragment<R, D> fragment : fragments) {
				IndexedArrayMapTask.Entry<D>[] stageFragment = fragment.stages[i];
				System.arraycopy(stageFragment, 0, stage, stagePos, stageFragment.length);
				stagePos += stageFragment.length;
				size += stageFragment.length;
			}

			stages[i] = stage;
		}


		return new StagedDataChunk<>(header.pos, header.name, stages, size);
	}
}
