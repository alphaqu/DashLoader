package dev.quantumfusion.dashloader.registry;


import dev.quantumfusion.dashloader.io.data.CacheInfo;
import dev.quantumfusion.dashloader.registry.data.StageData;
import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StepTask;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("FinalMethodInFinalClass")
public final class RegistryReader {
	private final StageData[] chunkData;

	// Holds an array of the exported dataChunks array values.
	private final Object[][] data;

	public RegistryReader(CacheInfo metadata, StageData[] data) {
		this.chunkData = data;
		this.data = new Object[metadata.chunks.size()][];
		for (int i = 0; i < metadata.chunks.size(); i++) {
			this.data[i] = new Object[metadata.chunks.get(i).size];
		}
	}

	public final void export(@Nullable Consumer<Task> taskConsumer) {
		StepTask task = new StepTask("Exporting", Integer.max(this.chunkData.length, 1));
		if (taskConsumer != null) {
			taskConsumer.accept(task);
		}

		for (StageData chunkData : chunkData) {
			chunkData.preExport(this);
			chunkData.export(data, this);
			chunkData.postExport(this);
		}
	}

	@SuppressWarnings("unchecked")
	public final <R> R get(final int pointer) {
		// inlining go brrr
		return (R) this.data[pointer & 0x3f][pointer >>> 6];
	}
}
