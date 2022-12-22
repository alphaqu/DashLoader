package dev.quantumfusion.dashloader.registry;


import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.registry.chunk.data.DataChunk;
import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StepTask;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("FinalMethodInFinalClass")
public final class RegistryReader {
	private final DataChunk<?, ?>[] dataChunks;

	// Holds an array of the exported dataChunks array values.
	private final Object[][] data;

	public RegistryReader(DataChunk<?, ?>[] data) {
		this.dataChunks = data;
		this.data = new Object[data.length][];
	}

	public final void export(@Nullable Consumer<Task> taskConsumer) {
		StepTask task = new StepTask("Exporting", Integer.max(this.dataChunks.length, 1));
		if (taskConsumer != null) {
			taskConsumer.accept(task);
		}

		for (int i = 0; i < this.dataChunks.length; i++) {
			var chunk = this.dataChunks[i];
			final int size = chunk.getSize();
			var dataObjects = new Object[size];
			this.data[i] = dataObjects;
			task.run(new StepTask(chunk.name, 3), (subTask) -> {
				DashLoader.LOG.info("Loading " + size + " " + chunk.name + "s");
				chunk.preExport(this);
				subTask.next();
				chunk.export(dataObjects, this);
				subTask.next();
				chunk.postExport(this);
				subTask.next();
			});
		}
	}

	@SuppressWarnings("unchecked")
	public final <R> R get(final int pointer) {
		// inlining go brrr
		return (R) this.data[pointer & 0x3f][pointer >>> 6];
	}
}
