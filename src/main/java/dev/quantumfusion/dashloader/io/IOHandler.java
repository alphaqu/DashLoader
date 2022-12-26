package dev.quantumfusion.dashloader.io;

import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.api.DashObjectClass;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.io.meta.CacheMetadata;
import dev.quantumfusion.dashloader.io.serializer.SimpleSerializer;
import dev.quantumfusion.dashloader.registry.RegistryFactory;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.data.ChunkData;
import dev.quantumfusion.dashloader.registry.data.StageData;
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
	private final Map<Class<?>, SimpleSerializer<?>> serializers = new HashMap<>();

	private final MetadataSerializer metadataSerializer = new MetadataSerializer();
	private final RegistrySerializer registrySerializer = new RegistrySerializer();

	private final Path cacheDir;

	private String cacheArea;
	private String subCacheArea;

	public IOHandler(Path cacheDir) {
		this.cacheDir = cacheDir;
	}

	public void init(List<DashObjectClass<?, ?>> dashObjects, int compressionLevel) {
		this.registrySerializer.init(dashObjects, compressionLevel);
	}

	@SafeVarargs
	public final void addSerializer(String name, Class<?> dataObject, List<DashObjectClass<?, ?>> dashObjects, Class<? extends Dashable<?>>... dashables) {
		this.serializers.put(dataObject, SimpleSerializer.create(name, this.getCurrentCacheDir(), dataObject, dashObjects, dashables));
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


	public void saveRegistry(RegistryFactory factory, Consumer<Task> taskConsumer) {
		Path dir = this.getCurrentSubCacheDir();
		try {
			CacheMetadata metadata = this.registrySerializer.serialize(dir, factory, taskConsumer);
			this.metadataSerializer.serialize(dir, metadata);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public RegistryReader loadRegistry() {
		Path dir = this.getCurrentSubCacheDir();
		try {
			CacheMetadata metadata = this.metadataSerializer.deserialize(dir);
			StageData[] chunks = this.registrySerializer.deserialize(dir, metadata, DashLoader.DL.api.getDashObjects());
			return new RegistryReader(metadata, chunks);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public <O> O load(Class<O> dataObject) {
		try {
			//noinspection unchecked
			return (O) this.serializers.get(dataObject).decode(this.getCurrentSubCacheDir());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public <O> void save(O dataObject, @Nullable Consumer<Task> task) {
		try {
			//noinspection unchecked
			((SimpleSerializer<O>) this.serializers.get(dataObject.getClass())).encode(dataObject, this.getCurrentSubCacheDir(), task);
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
