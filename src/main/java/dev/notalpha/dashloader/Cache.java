package dev.notalpha.dashloader;

import dev.notalpha.dashloader.api.DashEntrypoint;
import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.MissingHandler;
import dev.notalpha.dashloader.api.config.ConfigHandler;
import dev.notalpha.dashloader.io.MappingSerializer;
import dev.notalpha.dashloader.io.RegistrySerializer;
import dev.notalpha.dashloader.io.data.CacheInfo;
import dev.notalpha.dashloader.misc.ProfilerUtil;
import dev.notalpha.dashloader.registry.RegistryFactory;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.data.StageData;
import dev.quantumfusion.taski.builtin.StepTask;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Cache {
	private static final String METADATA_FILE_NAME = "metadata.bin";
	private Status status;
	private String hash;
	private final Path cacheDir;

	// DashLoader metadata
	private final List<DashModule<?>> cacheHandlers;
	private final List<DashObjectClass<?, ?>> dashObjects;

	// Serializers
	private final RegistrySerializer registrySerializer;
	private final MappingSerializer mappingsSerializer;

	Cache(Path cacheDir, List<DashModule<?>> cacheHandlers, List<DashObjectClass<?, ?>> dashObjects) {
		this.cacheDir = cacheDir;
		this.cacheHandlers = cacheHandlers;
		this.dashObjects = dashObjects;
		this.registrySerializer = new RegistrySerializer(dashObjects);
		this.mappingsSerializer = new MappingSerializer(cacheHandlers);
	}

	public void start() {
		if (this.exists()) {
			this.setStatus(Cache.Status.LOAD);
			this.load();
		} else {
			this.setStatus(Cache.Status.SAVE);
		}
	}

	public boolean save(@Nullable Consumer<StepTask> taskConsumer) {
		DashLoader.LOG.info("Starting DashLoader Caching");
		try {
			if (status != Status.SAVE) {
				throw new RuntimeException("Status is not SAVE");
			}

			Path ourDir = getDir();

			// Max caches
			int maxCaches = ConfigHandler.INSTANCE.config.maxCaches;
			if (maxCaches != -1) {
				DashLoader.LOG.info("Checking for cache count.");
				try {
					FileTime oldestTime = null;
					Path oldestPath = null;
					int cacheCount = 1;
					try (Stream<Path> stream = Files.list(cacheDir)) {
						for (Path path : stream.toList()) {
							if (!Files.isDirectory(path)) {
								continue;
							}

							if (path.equals(ourDir)) {
								continue;
							}
							cacheCount += 1;

							try {
								BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
								FileTime lastAccessTime = attrs.lastAccessTime();
								if (oldestTime == null || lastAccessTime.compareTo(oldestTime) < 0) {
									oldestTime = lastAccessTime;
									oldestPath = path;
								}
							} catch (IOException e) {
								DashLoader.LOG.warn("Could not find access time for cache.", e);
							}
						}
					}

					if (oldestPath != null && cacheCount > maxCaches) {
						DashLoader.LOG.info("Removing {} as we are currently above the maximum caches.", oldestPath);
						if (!FileUtils.deleteQuietly(oldestPath.toFile())) {
							DashLoader.LOG.error("Could not remove cache {}", oldestPath);
						}
					}
				} catch (IOException io) {
					DashLoader.LOG.error("Could not enforce maximum cache ", io);
				}
			}

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
			mappingsSerializer.save(ourDir, factory, cacheHandlers, main);
			main.next();

			// serialization
			main.run(new StepTask("serialize", 2), (task) -> {
				try {
					CacheInfo info = this.registrySerializer.serialize(ourDir, factory, task::setSubTask);
					task.next();
					DashLoader.METADATA_SERIALIZER.save(ourDir.resolve(METADATA_FILE_NAME), new StepTask("hi"), info);
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
			this.clear();
			return false;
		}
	}

	public void load() {
		this.status = Status.LOAD;
		long start = System.currentTimeMillis();
		try {
			StepTask task = new StepTask("Loading DashCache", 3);
			Path cacheDir = getDir();

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
				this.clear();
				return;
			}

			DashLoader.LOG.info("Loaded cache in {}", ProfilerUtil.getTimeStringFromStart(start));
		} catch (Exception e) {
			DashLoader.LOG.error("Summoned CrashLoader in {}", ProfilerUtil.getTimeStringFromStart(start), e);
			this.setStatus(Status.SAVE);
			this.clear();
		}
	}

	public void setHash(String hash) {
		DashLoader.LOG.info("Hash changed to " + hash);
		this.hash = hash;
	}

	public boolean exists() {
		return Files.exists(this.getDir());
	}

	public void clear() {
		try {
			FileUtils.deleteDirectory(this.getDir().toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Path getDir() {
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
			DashLoader.LOG.info("\u001B[46;30m DashLoader Status change {}\n\u001B[49;39m", status);
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
