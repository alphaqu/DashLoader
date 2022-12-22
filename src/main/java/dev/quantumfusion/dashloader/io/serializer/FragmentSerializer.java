package dev.quantumfusion.dashloader.io.serializer;

import dev.quantumfusion.dashloader.registry.ChunkHolder;
import dev.quantumfusion.taski.Task;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public class FragmentSerializer<C extends ChunkHolder> implements DataSerializer<C> {
	@Override
	public void encode(C object, Path subCache, @Nullable Consumer<Task> task) throws IOException {

	}

	@Override
	public C decode(Path subCache, @Nullable Consumer<Task> task) throws IOException {
		return null;
	}
}
