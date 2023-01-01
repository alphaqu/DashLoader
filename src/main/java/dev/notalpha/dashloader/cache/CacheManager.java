package dev.notalpha.dashloader.cache;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.DashCacheHandler;
import dev.notalpha.dashloader.api.DashEntrypoint;
import dev.notalpha.dashloader.cache.io.MappingSerializer;
import dev.notalpha.dashloader.cache.io.RegistrySerializer;
import dev.notalpha.dashloader.cache.io.data.CacheInfo;
import dev.notalpha.dashloader.cache.registry.RegistryFactory;
import dev.notalpha.dashloader.cache.registry.RegistryReader;
import dev.notalpha.dashloader.cache.registry.data.StageData;
import dev.notalpha.dashloader.cache.registry.factory.MissingHandler;
import dev.notalpha.dashloader.util.ProfilerUtil;
import dev.quantumfusion.taski.builtin.StepTask;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class CacheManager {
	private static final String METADATA_FILE_NAME = "metadata.bin";
	private Status status;
	private String hash;
	private final Path cacheDir;

	// DashLoader metadata
	private final List<DashCacheHandler<?>> cacheHandlers;
	private final List<DashObjectClass<?, ?>> dashObjects;

	// Serializers
	private final RegistrySerializer registrySerializer;
	private final MappingSerializer mappingsSerializer;

	CacheManager(Path cacheDir, List<DashCacheHandler<?>> cacheHandlers, List<DashObjectClass<?, ?>> dashObjects) {
		this.cacheDir = cacheDir;
		this.cacheHandlers = cacheHandlers;
		this.dashObjects = dashObjects;
		this.registrySerializer = new RegistrySerializer(dashObjects);
		this.mappingsSerializer = new MappingSerializer(cacheHandlers);
	}

	public void start() {
		if (this.cacheExists()) {
			this.setStatus(CacheManager.Status.LOAD);
			this.loadCache();
		} else {
			this.setStatus(CacheManager.Status.SAVE);
		}
	}

	public boolean saveCache(@Nullable Consumer<StepTask> taskConsumer) {
		if (status != Status.SAVE) {
			throw new RuntimeException("Status is not SAVE");
		}
		DashLoader.LOG.info("Starting DashLoader Caching");
		try {
			long start = System.currentTimeMillis();

			StepTask main = new StepTask("save", 2);
			if (taskConsumer != null) {
				taskConsumer.accept(main);
			}

			// Setup handlers
			List<MissingHandler<?>> handlers = new ArrayList<>();
			for (DashEntrypoint entryPoint : FabricLoader.getInstance().getEntrypoints("dashloader", DashEntrypoint.class)) {
				entryPoint.onDashLoaderSave(handlers);
			}
			RegistryFactory factory = RegistryFactory.create(handlers, dashObjects);

			// Mappings
			mappingsSerializer.save(getCacheDir(), factory, cacheHandlers, main);
			main.next();

			// serialization
			main.run(new StepTask("serialize", 2), (task) -> {
				try {
					CacheInfo info = this.registrySerializer.serialize(getCacheDir(), factory, task::setSubTask);
					task.next();
					DashLoader.METADATA_SERIALIZER.save(getCacheDir().resolve(METADATA_FILE_NAME), new StepTask("hi"), info);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				task.next();
			});

			DashLoader.LOG.info("Saved cache in " + ProfilerUtil.getTimeStringFromStart(start));
			return true;
		} catch (Throwable thr) {
			DashLoader.LOG.error("Failed caching", thr);
			this.setStatus(Status.SAVE);
			this.clearCache();
			return false;
		}
	}

	public void loadCache() {
		this.status = Status.LOAD;
		long start = System.currentTimeMillis();
		try {
			StepTask task = new StepTask("Loading DashCache", 3);
			Path cacheDir = getCacheDir();

			// Get metadata
			Path metadataPath = cacheDir.resolve(METADATA_FILE_NAME);
			CacheInfo info = DashLoader.METADATA_SERIALIZER.load(metadataPath);

			// File reading
			StageData[] stageData = registrySerializer.deserialize(cacheDir, info, dashObjects);
			RegistryReader reader = new RegistryReader(info, stageData);

			// Exporting assets
			task.run(() -> {
				reader.export(task::setSubTask);
			});

			// Loading mappings
			if (!mappingsSerializer.load(cacheDir, reader, cacheHandlers)) {
				this.setStatus(Status.SAVE);
				this.clearCache();
				return;
			}

			DashLoader.LOG.info("Loaded cache in {}", ProfilerUtil.getTimeStringFromStart(start));
		} catch (Exception e) {
			DashLoader.LOG.error("Summoned CrashLoader in {}", ProfilerUtil.getTimeStringFromStart(start), e);
			this.setStatus(Status.SAVE);
			this.clearCache();
		}
	}

	public void setHash(String hash) {
		DashLoader.LOG.info("Hash changed to " + hash);
		this.hash = hash;
	}

	public boolean cacheExists() {
		return Files.exists(this.getCacheDir());
	}

	public void clearCache() {
		try {
			FileUtils.deleteDirectory(this.getCacheDir().toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Path getCacheDir() {
		if (hash == null) {
			throw new RuntimeException("Cache hash has not been set.");
		}
		return cacheDir.resolve(hash + "/");
	}


	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		if (this.status != status) {
			this.status = status;
			DashLoader.LOG.info("\u001B[46m\u001B[30m DashLoader Status change {}\n\u001B[0m", status);
			this.cacheHandlers.forEach(handler -> handler.reset(this));
		}
	}

	public enum Status {
		/**
		 * Idle
		 */
		IDLE,
		/**
		 * The cache manager is in the process of loading a cache.
		 */
		LOAD,
		/**
		 * The cache manager is creating a cache.
		 */
		SAVE,
	}


}
