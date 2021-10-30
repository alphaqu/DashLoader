package dev.quantumfusion.dashloader.def;

import dev.quantumfusion.dashloader.core.DashLoaderCore;
import dev.quantumfusion.dashloader.core.registry.ChunkDataHolder;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.api.DashLoaderAPI;
import dev.quantumfusion.dashloader.def.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.def.data.VanillaData;
import dev.quantumfusion.dashloader.def.data.blockstate.DashBlockState;
import dev.quantumfusion.dashloader.def.data.blockstate.property.DashProperty;
import dev.quantumfusion.dashloader.def.data.blockstate.property.value.DashPropertyValue;
import dev.quantumfusion.dashloader.def.data.dataobject.ImageData;
import dev.quantumfusion.dashloader.def.data.dataobject.MappingData;
import dev.quantumfusion.dashloader.def.data.dataobject.ModelData;
import dev.quantumfusion.dashloader.def.data.dataobject.RegistryData;
import dev.quantumfusion.dashloader.def.data.font.DashFont;
import dev.quantumfusion.dashloader.def.data.image.DashImage;
import dev.quantumfusion.dashloader.def.data.image.DashSprite;
import dev.quantumfusion.dashloader.def.data.image.shader.DashShader;
import dev.quantumfusion.dashloader.def.data.model.DashModel;
import dev.quantumfusion.dashloader.def.data.model.components.DashBakedQuad;
import dev.quantumfusion.dashloader.def.data.model.predicates.DashPredicate;
import dev.quantumfusion.hyphen.SerializerFactory;
import dev.quantumfusion.hyphen.io.ByteBufferIO;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;


public class DashLoader {
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
	private DashMetadata metadata;
	private DashLoaderCore core;

	public DashLoader(ClassLoader classLoader) {
		try {
			LOGGER.info("Initializing DashLoader " + VERSION + ".");
			instance = this;
			final FabricLoader instance = FabricLoader.getInstance();

			this.api = new DashLoaderAPI(this);
			api.initAPI();

			metadata = new DashMetadata();
			metadata.setModHash(instance);
			this.core = new DashLoaderCore(MAIN_PATH.resolve("mods-" + metadata.modInfo + "/"), api.dashObjects.toArray(Class[]::new));
			if (instance.isDevelopmentEnvironment()) {
				LOGGER.warn("DashLoader launched in dev.");
			}
			core.setCurrentSubcache("null");

			core.prepareSerializer(RegistryData.class, DashBlockState.class, DashFont.class, DashIdentifierInterface.class, DashProperty.class, DashPropertyValue.class, DashSprite.class, DashPredicate.class, DashBakedQuad.class);
			core.prepareSerializer(ImageData.class, DashImage.class);
			core.prepareSerializer(ModelData.class, DashModel.class);
			core.prepareSerializer(MappingData.class);

			LOGGER.info("Created DashLoader with {} classloader.", classLoader.getClass().getSimpleName());
			LOGGER.info("Initialized DashLoader");

			final SerializerFactory<ByteBufferIO, DashShader> factory = SerializerFactory.createDebug(ByteBufferIO.class, DashShader.class);
			factory.setExportPath(Path.of("./ForFucksSake.class"));
			factory.setClassName("ForFucksSake");
			factory.build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("mc exception bad");
		}
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

	public MappingData getMappings() {
		return mappings;
	}

	public DashLoaderAPI getApi() {
		return api;
	}

	public void requestReload() {
		shouldReload = true;
	}

	public void reload(Collection<String> resourcePacks) {
		if (shouldReload) {
			metadata.setResourcePackHash(resourcePacks);
			core.setCurrentSubcache(metadata.resourcePacks);
			LOGGER.info("Reloading DashLoader. [mod-hash: {}] [resource-hash: {}]", metadata.modInfo, metadata.resourcePacks);
			if (core.isCacheMissing()) status = Status.EMPTY;
			else loadDashCache();
			LOGGER.info("Reloaded DashLoader");
			shouldReload = false;
		}
	}

	public void saveDashCache() {
		LOGGER.info("Creating writer");
		DashRegistryWriter writer = core.createWriter();
		LOGGER.info("Creating Mapped data");
		MappingData mappings = new MappingData();
		LOGGER.info("Writing vanilla data");
		mappings.writeVanillaData(VANILLA_DATA, writer, TASK_HANDLER);
		LOGGER.info("Saving registry data");
		core.save(new RegistryData(writer));
		LOGGER.info("Saving image data");
		core.save(new ImageData(writer));
		LOGGER.info("Saving model data");
		core.save(new ModelData(writer));
		LOGGER.info("Saving mapping data");
		core.save(mappings);
		TASK_HANDLER.setCurrentTask("Caching is now complete.");
		LOGGER.info("Created cache in " + "TODO" + "s");
	}

	public void loadDashCache() {
		core.setCurrentSubcache(metadata.resourcePacks);
		LOGGER.info("Starting DashLoader Deserialization");
		try {

			AtomicReference<MappingData> mappingsReference = new AtomicReference<>();
			ChunkDataHolder[] registryDataObjects = new ChunkDataHolder[3];


			registryDataObjects[0] = (core.load(RegistryData.class));
			registryDataObjects[1] = (core.load(ImageData.class));
			registryDataObjects[2] = (core.load(ModelData.class));
			mappingsReference.set(core.load(MappingData.class));

			mappings = mappingsReference.get();
			assert mappings != null;

			LOGGER.info("      Creating Registry");
			final DashRegistryReader reader = core.createReader(registryDataObjects);

			LOGGER.info("      Loading Mappings");
			mappings.export(reader, VANILLA_DATA);


			LOGGER.info("    Loaded DashLoader");
			status = Status.LOADED;
		} catch (Exception e) {
			LOGGER.error("DashLoader has devolved to CrashLoader???", e);
			status = Status.CRASHLOADER;
			if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
				// TODO REMOVE FILES IF IT CRASHED
			}
		}
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
