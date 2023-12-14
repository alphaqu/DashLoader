package dev.notalpha.dashloader.client.fs.input;

import net.minecraft.resource.InputSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathInputSupplier implements InputSupplier<InputStream> {
	private final Path path;

	public PathInputSupplier(Path path) {
		this.path = path;
	}

	@Override
	public InputStream get() throws IOException {
		return Files.newInputStream(path);
	}
}
