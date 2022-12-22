package dev.quantumfusion.dashloader.io.serializer;

import dev.quantumfusion.taski.Task;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public interface DataSerializer<O> {
	void encode(O object, Path subCache, @Nullable Consumer<Task> task) throws IOException;
	O decode(Path subCache, @Nullable Consumer<Task> task) throws IOException;
}
