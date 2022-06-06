package dev.quantumfusion.dashloader.io;

import dev.quantumfusion.dashloader.DashObjectClass;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.io.serializer.DashSerializer;
import dev.quantumfusion.taski.Task;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The IO Module of DashLoaderCore. Handles Serializers and Caches.
 */
public final class IOHandler {
	private final Map<Class<?>, DashSerializer<?>> serializers = new HashMap<>();

	private final Path cacheDir;

	private String cacheArea;
	private String subCacheArea;

	public IOHandler(Path cacheDir) {
		this.cacheDir = cacheDir;
	}

	@SafeVarargs
	public final void addSerializer(Class<?> dataObject, List<DashObjectClass<?, ?>> dashObjects, Class<? extends Dashable<?>>... dashables) {
		this.serializers.put(dataObject, DashSerializer.create(this.getCurrentCacheDir(), dataObject, dashObjects, dashables));
	}

	public void setCacheArea(String name) {
		this.cacheArea = name;
	}

	public void setSubCacheArea(String name) {
		this.subCacheArea = name;
	}

	public void clearCache() {
		try {
			FileUtils.deleteDirectory(this.getCurrentSubCacheDir().toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean cacheExists() {
		return Files.exists(this.getCurrentSubCacheDir());
	}

	public <O> O load(Class<O> dataObject, @Nullable Consumer<Task> task) {
		try {
			return (O) this.serializers.get(dataObject).decode(this.getCurrentSubCacheDir(), task);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public <O> void save(O dataObject, @Nullable Consumer<Task> task) {
		try {
			((DashSerializer<O>) this.serializers.get(dataObject.getClass())).encode(dataObject, this.getCurrentSubCacheDir(), task);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Path getCurrentSubCacheDir() {
		if (this.subCacheArea == null) {
			throw new RuntimeException("Current SubCache has not been set.");
		}
		return this.getCurrentCacheDir().resolve(this.subCacheArea + "/");
	}

	private Path getCurrentCacheDir() {
		if (this.cacheArea == null) {
			throw new RuntimeException("Current Cache has not been set.");
		}
		return this.cacheDir.resolve(this.cacheArea + "/");
	}
}
