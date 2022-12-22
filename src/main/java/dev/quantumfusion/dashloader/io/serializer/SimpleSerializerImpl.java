package dev.quantumfusion.dashloader.io.serializer;

import dev.quantumfusion.taski.Task;

import java.io.IOException;
import java.nio.file.Path;

public class SimpleSerializerImpl implements DataSerializer {
	@Override
	public void encode(Object object, Path subCache, Task parent) throws IOException {

	}

	@Override
	public Object decode(Path subCache) throws IOException {
		return null;
	}
}
