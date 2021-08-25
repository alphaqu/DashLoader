package net.oskarstrom.dashloader.def;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;
import net.oskarstrom.dashloader.api.DashLoaderFactory;
import net.oskarstrom.dashloader.api.ThreadManager;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.registry.Pointer;
import net.oskarstrom.dashloader.api.serializer.DashSerializer;
import net.oskarstrom.dashloader.api.serializer.DashSerializerManager;
import net.oskarstrom.dashloader.core.util.ClassLoaderHelper;
import net.oskarstrom.dashloader.def.api.DashDataType;
import net.oskarstrom.dashloader.def.api.DashLoaderAPI;
import net.oskarstrom.dashloader.def.data.DashSerializers;
import net.oskarstrom.dashloader.def.data.VanillaData;
import net.oskarstrom.dashloader.def.data.serialize.*;
import net.oskarstrom.dashloader.def.registry.PropertyValueRegistryStorage;
import net.oskarstrom.dashloader.def.util.enums.DashCachePaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class DashLoader implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("DashLoader");
	public static final String VERSION = FabricLoader.getInstance()
			.getModContainer("dashloader")
			.orElseThrow(() -> new IllegalStateException("DashLoader not found... apparently! WTF?"))
			.getMetadata()
			.getVersion()
			.getFriendlyString();

	public static final TaskHandler TASK_HANDLER = new TaskHandler(LOGGER);
	private static final Path MAIN_PATH = FabricLoader.getInstance().getConfigDir().normalize().resolve("quantumfusion/dashloader/");
	private static final VanillaData VANILLA_DATA = new VanillaData();
	private static boolean shouldReload = true;
	private static DashLoader instance;
	private final DashLoaderAPI api;
	private Status status;
	private MappingData mappings;
	private DashSerializerManager serializerManager;
	private DashMetadata metadata;

	public DashLoader(ClassLoader classLoader) {
		instance = this;
		this.api = new DashLoaderAPI(this);
		ClassLoaderHelper.accessor = new ClassLoaderHelper.Accessor(classLoader);
		LOGGER.info("Created DashLoader with {} classloader.", classLoader.getClass().getSimpleName());
	}

	public static Path getMainPath() {
		return MAIN_PATH;
	}

	public static DashLoader getInstance() {
		return instance;
	}

	public static VanillaData getVanillaData() {
		return VANILLA_DATA;
	}

	private static <O> O deserialize(DashSerializer<O> serializer, DashCachePaths path) {
		try {
			return serializer.deserialize(path.getFileName(true));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public MappingData getMappings() {
		return mappings;
	}

	public DashLoaderAPI getApi() {
		return api;
	}

	public void requestReload() {
		shouldReload = true;
	}

	public void initialize() {
		LOGGER.info("Initializing DashLoader " + VERSION + ".");
		final FabricLoader instance = FabricLoader.getInstance();
		if (instance.isDevelopmentEnvironment()) {
			LOGGER.warn("DashLoader launched in dev.");
		}
		metadata = new DashMetadata();
		metadata.setModHash(instance);
		serializerManager = DashLoaderFactory.createSerializationManager(getModBoundDir());
		ThreadManager.init();
		LOGGER.info("Initialized DashLoader");
	}

	public void reload(Collection<String> resourcePacks) {
		if (shouldReload) {
			metadata.setResourcePackHash(resourcePacks);
			LOGGER.info("Reloading DashLoader. [mod-hash: {}] [resource-hash: {}]", metadata.modInfo, metadata.resourcePacks);
			if (Arrays.stream(DashCachePaths.values()).allMatch(dashCachePaths -> Files.exists(getResourcePackBoundDir().resolve(dashCachePaths.getFileName(true))))) {
				loadDashCache();
			} else {
				status = Status.EMPTY;
			}
			LOGGER.info("Reloaded DashLoader");
			shouldReload = false;
		}
	}

	public void saveDashCache() {
		DashRegistry registry = DashLoaderFactory.createSerializationRegistry((o, registri) -> {
			if (o instanceof Enum<?> enumObject) {
				final byte storagePointer = api.storageMappings.getByte(DashDataType.PROPERTY_VALUE);
				return new Pointer(((PropertyValueRegistryStorage) registri.getStorage(storagePointer)).add(enumObject), storagePointer);
			}
			return null;
		});
		api.initAPI(registry);


		MappingData mappings = new MappingData();
		mappings.loadVanillaData(VANILLA_DATA, registry, TASK_HANDLER);


		try {
			DashSerializers.REGISTRY_SERIALIZER.serialize(DashCachePaths.REGISTRY_CACHE.getFileName(true), new RegistryData(registry));
			DashSerializers.IMAGE_SERIALIZER.serialize(DashCachePaths.REGISTRY_IMAGE_CACHE.getFileName(true), new ImageData(registry));
			DashSerializers.MODEL_SERIALIZER.serialize(DashCachePaths.REGISTRY_MODEL_CACHE.getFileName(true), new ModelData(registry));
			DashSerializers.MAPPING_SERIALIZER.serialize(DashCachePaths.MAPPINGS_CACHE.getFileName(true), mappings);
		} catch (IOException e) {
			e.printStackTrace();
		}

		TASK_HANDLER.setCurrentTask("Caching is now complete.");
		LOGGER.info("Created cache in " + "TODO" + "s");

	}

	public void loadDashCache() {
		LOGGER.info("Starting DashLoader Deserialization");
		try {
			DashSerializers.initSerializers();
			AtomicReference<MappingData> mappingsReference = new AtomicReference<>();
			List<RegistryDataObject> registryDataObjects = new ArrayList<>();

			ThreadManager.executeRunnables(
					() -> registryDataObjects.add(deserialize(DashSerializers.REGISTRY_SERIALIZER, DashCachePaths.REGISTRY_CACHE)),
					() -> registryDataObjects.add(deserialize(DashSerializers.MODEL_SERIALIZER, DashCachePaths.REGISTRY_MODEL_CACHE)),
					() -> registryDataObjects.add(deserialize(DashSerializers.IMAGE_SERIALIZER, DashCachePaths.REGISTRY_IMAGE_CACHE)),
					() -> mappingsReference.set(deserialize(DashSerializers.MAPPING_SERIALIZER, DashCachePaths.MAPPINGS_CACHE))
			);
			mappings = mappingsReference.get();
			assert mappings != null;

			LOGGER.info("      Creating Registry");
			//get the total amount of registrystorages
			int size = 0;
			for (RegistryDataObject registryDataObject : registryDataObjects)
				size += registryDataObject.getSize();

			//create the registry
			DashRegistry registry = DashLoaderFactory.createDeserializationRegistry(size);

			LOGGER.info("      Loading Registry");
			registry.apply(registry);

			LOGGER.info("      Loading Mappings");
			mappings.toUndash(registry, VANILLA_DATA);


			LOGGER.info("    Loaded DashLoader");
			status = Status.LOADED;
		} catch (Exception e) {
			LOGGER.error("DashLoader has devolved to CrashLoader???", e);
			status = Status.CRASHLOADER;
			if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
				try {
					Files.deleteIfExists(getModBoundDir());
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}
		}
	}

	public Path getModBoundDir() {
		try {
			final Path resolve = DashLoader.getMainPath().resolve("mods-" + metadata.modInfo + "/");
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

	@Override
	public void onInitialize() {
		//TODO timing
	}

	public DashSerializerManager getSerializerManager() {
		return serializerManager;
	}

	public Status getStatus() {
		return status;
	}


	public enum Status {
		LOADED,
		CRASHLOADER,
		EMPTY
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
