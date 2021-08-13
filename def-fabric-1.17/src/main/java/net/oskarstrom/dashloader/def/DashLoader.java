package net.oskarstrom.dashloader.def;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;
import net.oskarstrom.dashloader.core.DashLoaderManager;
import net.oskarstrom.dashloader.core.util.ClassLoaderHelper;
import net.oskarstrom.dashloader.def.data.DashSerializers;
import net.oskarstrom.dashloader.def.data.VanillaData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;


public class DashLoader {
	public static final Logger LOGGER = LogManager.getLogger("DashLoader");
	public static final String VERSION = FabricLoader.getInstance().getModContainer("dashloader").get().getMetadata().getVersion().getFriendlyString();
	public static final TaskHandler TASK_HANDLER = new TaskHandler(LOGGER);
	private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().normalize();
	private static final VanillaData VANILLA_DATA = new VanillaData();
	public static ForkJoinPool THREAD_POOL;
	private static boolean shouldReload = true;
	private static DashLoader instance;
	private final DashLoaderManager coreManager;
	private DashMetadata metadata;

	public DashLoader(ClassLoader classLoader) {
		instance = this;
		this.coreManager = new DashLoaderManager(getModBoundDir());
		ClassLoaderHelper.setAccessor(classLoader);
		LOGGER.info("Created DashLoader with {} classloader.", classLoader.getClass().getSimpleName());
	}

	public static Path getConfig() {
		return CONFIG;
	}

	public static DashLoader getInstance() {
		return instance;
	}

	public static VanillaData getVanillaData() {
		return VANILLA_DATA;
	}



	public DashLoaderManager getCoreManager() {
		return coreManager;
	}

	public void requestReload() {
		shouldReload = true;
	}

	public void initialize() {
		Instant start = Instant.now();
		LOGGER.info("Initializing DashLoader " + VERSION + ".");
		final FabricLoader instance = FabricLoader.getInstance();
		if (instance.isDevelopmentEnvironment()) {
			LOGGER.warn("DashLoader launched in dev.");
		}
		metadata = new DashMetadata();
		metadata.setModHash(instance);
		initThreadPool();
		LOGGER.info("Initialized DashLoader");
	}

	public void reload(Collection<String> resourcePacks) {
		if (shouldReload) {
			if (THREAD_POOL.isTerminated()) {
				initThreadPool();
			}
			metadata.setResourcePackHash(resourcePacks);
			LOGGER.info("Reloading DashLoader. [mod-hash: {}] [resource-hash: {}]", metadata.modInfo, metadata.resourcePacks);
			DashSerializers.initSerializers();
			if (Arrays.stream(DashCachePaths.values()).allMatch(dashCachePaths -> Files.exists(dashCachePaths.getPath()))) {
				loadDashCache();
			}
			shutdownThreadPool();
			LOGGER.info("Reloaded DashLoader");
			shouldReload = false;
		}
	}

	public void loadDashCache() {
		LOGGER.info("Starting DashLoader Deserialization");
		try {
			DashRegistry registry = new DashRegistry();
			ThreadHelper.exec(
					() -> DashSerializers.REGISTRY_SERIALIZER.deserializeObject("Cache").dumpData(registry),
					() -> DashSerializers.MODEL_SERIALIZER.deserializeObject("Model Cache").dumpData(registry),
					() -> DashSerializers.IMAGE_SERIALIZER.deserializeObject("Image Cache").dumpData(registry),
					() -> mappings = DashSerializers.MAPPING_SERIALIZER.deserializeObject("Mapping")
			);
			assert mappings != null;


			LOGGER.info("      Loading Registry");
			registry.toUndash();

			LOGGER.info("      Loading Mappings");
			mappings.toUndash(registry, VANILLA_DATA);


			LOGGER.info("    Loaded DashLoader");
			state = DashCacheState.LOADED;
		} catch (Exception e) {
			state = DashCacheState.CRASHLOADER;
			LOGGER.error("DashLoader has devolved to CrashLoader???", e);
			if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
				try {
					Files.deleteIfExists(getModBoundDir());
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}
		}
	}


	public void saveDashCache() {
		Instant start = Instant.now();
		TASK_HANDLER.reset();
		TaskHandler.setTotalTasks(FeatureHandler.calculateTasks());
		initThreadPool();
		coreManager.initAPI();
		TASK_HANDLER.completedTask();
		final List<DashDataClass> dataClasses = coreManager.dataClasses;
		dataClasses.forEach(DashDataClass::saveInit);
		DashRegistry registry = new DashRegistry();
		DashMappings mappings = new DashMappings();
		registryForEach(dataClasses, registry, DashDataClass::saveReload);
		mappings.loadVanillaData(VANILLA_DATA, registry, TASK_HANDLER);
		final Map<Class<?>, DashDataType> apiFailed = registry.apiFailed;
		if (apiFailed.size() == 0) {
			registryForEach(dataClasses, registry, DashDataClass::saveApply);
			DashSerializers.REGISTRY_SERIALIZER.serializeObject(new DashRegistryData(registry), "Cache");
			DashSerializers.IMAGE_SERIALIZER.serializeObject(new RegistryImageData(registry.images), "Image Cache");
			DashSerializers.MODEL_SERIALIZER.serializeObject(new RegistryModelData(registry.models), "Model Cache");
			DashSerializers.MAPPING_SERIALIZER.serializeObject(mappings, "Mapping");
			shutdownThreadPool();
			TASK_HANDLER.setCurrentTask("Caching is now complete.");
			LOGGER.info("Created cache in " + TimeHelper.getDecimalS(start, Instant.now()) + "s");
		} else {
			TASK_HANDLER.setCurrentTask("Incompatible mods found, Please check logs.");
			TASK_HANDLER.failed = true;
			registry.apiReport(LOGGER);
		}
	}


	private void initThreadPool() {
		if (THREAD_POOL == null || THREAD_POOL.isTerminated()) {
			final ForkJoinPool.ForkJoinWorkerThreadFactory factory = ForkJoinPool.defaultForkJoinWorkerThreadFactory;
			final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (thread, exception) -> LOGGER.fatal("Thread {} failed. Reason: ", thread.getName(), exception);
			THREAD_POOL = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), pool -> {
				final ForkJoinWorkerThread worker = factory.newThread(pool);
				worker.setName("dashloader-thread-" + worker.getPoolIndex());
				worker.setContextClassLoader(classLoader);
				return worker;
			}, uncaughtExceptionHandler, true);
		}

	}

	private void shutdownThreadPool() {
		THREAD_POOL.shutdown();
	}


	public Path getModBoundDir() {
		try {
			final Path resolve = DashLoader.getConfig().resolve("quantumfusion/dashloader/mods-" + metadata.modInfo + "/");
			return Files.createDirectories(resolve);
		} catch (IOException e) {
			LOGGER.error("Could not create ModBoundDir: ", e);
		}
		throw new IllegalStateException();
	}

	public Path getResourcePackBoundDir() {
		try {
			final Path resolve = getModBoundDir().resolve("resourcepacks-" + metadata.resourcePacks + "/");
			return Files.createDirectories(resolve);
		} catch (IOException e) {
			LOGGER.error("Could not create ResourcePackBoundDir: ", e);
		}
		throw new IllegalStateException();
	}


	public static class TaskHandler {
		public static int TOTALTASKS = 9;
		private static float taskStep = 1f / TOTALTASKS;
		private final Logger logger;
		private String task;
		private int tasksComplete;
		private int subTotalTasks = 1;
		private int subTasksComplete = 0;
		private boolean failed = false;

		public TaskHandler(Logger logger) {
			task = "Starting DashLoader";
			tasksComplete = 0;
			this.logger = logger;
		}

		public static void setTotalTasks(int tasks) {
			TOTALTASKS = tasks;
			taskStep = 1f / TOTALTASKS;
		}

		public void logAndTask(String s) {
			logger.info(s);
			tasksComplete++;
			task = s;
		}

		public void reset() {
			tasksComplete = 0;
			subTotalTasks = 1;
			subTasksComplete = 0;
		}

		public void completedTask() {
			tasksComplete++;
		}

		public void setCurrentTask(String task) {
			this.task = task;
		}

		public void setSubtasks(int tasks) {
			subTotalTasks = tasks;
			subTasksComplete = 0;
		}

		public void completedSubTask() {
			subTasksComplete++;
		}

		public Text getText() {
			return Text.of("(" + tasksComplete + "/" + TOTALTASKS + ") " + task);
		}

		public Text getSubText() {
			return TOTALTASKS == tasksComplete ? Text.of("") : Text.of("[" + subTasksComplete + "/" + subTotalTasks + "] ");
		}

		public boolean isFailed() {
			return failed;
		}

		public double getProgress() {
			if (failed) return 0;
			if (subTasksComplete == subTotalTasks && tasksComplete == TOTALTASKS) return 1;
			return (tasksComplete == 0 ? 0 : tasksComplete / (float) TOTALTASKS) + (((float) subTasksComplete / subTotalTasks) * taskStep);
		}
	}

	public static class DashMetadata {
		public String modInfo;
		public String resourcePacks;

		public void setModHash(FabricLoader loader) {
			long modInfoData = 420;
			for (ModContainer mod : loader.getAllMods()) {
				for (char c : mod.getMetadata().getVersion().getFriendlyString().toCharArray()) {
					modInfoData += c;
				}
			}
			this.modInfo = Long.toHexString(modInfoData + 0x69);
		}

		public void setResourcePackHash(Collection<String> resourcePacks) {
			long resourcePackData = 420;
			for (String resourcePack : resourcePacks) {
				for (char c : resourcePack.toCharArray()) {
					resourcePackData += c;
				}
			}
			this.resourcePacks = Long.toHexString(resourcePackData + 0x69);
		}
	}
}
