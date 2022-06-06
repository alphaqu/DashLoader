package dev.quantumfusion.dashloader;

import dev.quantumfusion.dashloader.api.DashLoaderAPI;
import dev.quantumfusion.dashloader.api.hook.LoadCacheHook;
import dev.quantumfusion.dashloader.api.hook.SaveCacheHook;
import dev.quantumfusion.dashloader.client.DashCachingScreen;
import dev.quantumfusion.dashloader.config.ConfigHandler;
import dev.quantumfusion.dashloader.corehook.*;
import dev.quantumfusion.dashloader.data.DashIdentifier;
import dev.quantumfusion.dashloader.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.data.DashModelIdentifier;
import dev.quantumfusion.dashloader.data.blockstate.DashBlockState;
import dev.quantumfusion.dashloader.data.font.DashFont;
import dev.quantumfusion.dashloader.data.image.DashImage;
import dev.quantumfusion.dashloader.data.image.DashSprite;
import dev.quantumfusion.dashloader.data.model.DashModel;
import dev.quantumfusion.dashloader.data.model.components.DashBakedQuad;
import dev.quantumfusion.dashloader.data.model.predicates.DashPredicate;
import dev.quantumfusion.dashloader.fallback.model.DashMissingDashModel;
import dev.quantumfusion.dashloader.io.IOHandler;
import dev.quantumfusion.dashloader.progress.ProgressHandler;
import dev.quantumfusion.dashloader.registry.ChunkHolder;
import dev.quantumfusion.dashloader.registry.RegistryHandler;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.thread.ThreadHandler;
import dev.quantumfusion.dashloader.util.TimeUtil;
import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StageTask;
import dev.quantumfusion.taski.builtin.StaticTask;
import dev.quantumfusion.taski.builtin.StepTask;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class DashLoader {
	public static final Logger LOGGER = LogManager.getLogger("DashLoader");
	public static final String VERSION = FabricLoader.getInstance()
			.getModContainer("dashloader")
			.orElseThrow(() -> new IllegalStateException("DashLoader not found... apparently! WTF?"))
			.getMetadata()
			.getVersion()
			.getFriendlyString();
	public static final Path DASH_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().normalize().resolve("dashloader.json");
	public static final Path DASH_CACHE_DIR = Path.of("./dashloader-cache/");
	public static final DashLoader INSTANCE = new DashLoader();
	public static long RELOAD_START = 0;
	public static long EXPORT_TIME = -1;
	public static long EXPORT_READING_TIME = -1;
	public static long EXPORT_EXPORTING_TIME = -1;
	public static long EXPORT_LOADING_TIME = -1;
	private static Status STATUS = Status.NONE;
	private boolean shouldReload = true;
	private final DashMetadata metadata = new DashMetadata();
	private final DashLoaderAPI api = new DashLoaderAPI();
	private DashDataManager dataManager;

	// Handlers
	public ConfigHandler config;
	public RegistryHandler registry;
	public ThreadHandler thread;
	public ProgressHandler progress;
	public IOHandler io;

	private DashLoader() {
	}

	public static void prepare() {
		LOGGER.info("Preparing DashLoader " + VERSION + ".");
		INSTANCE.prepareInternal();
	}

	public static void init() {
		LOGGER.info("Initializing DashLoader " + VERSION + ".");
		INSTANCE.initInternal(Thread.currentThread().getContextClassLoader());
	}

	private void prepareInternal() {
		this.metadata.setModHash(FabricLoader.getInstance());
		this.io = new IOHandler(DASH_CACHE_DIR);
		this.config = new ConfigHandler(DASH_CONFIG_PATH);
		this.thread = new ThreadHandler();
		this.progress = new ProgressHandler();
	}

	private void initInternal(ClassLoader classLoader) {
		try {
			this.api.initAPI();

			var dashObjects = parseDashObjects(this.api.dashObjects);
			this.registry = new RegistryHandler(dashObjects);
			this.config.reloadConfig();

			final FabricLoader instance = FabricLoader.getInstance();
			if (instance.isDevelopmentEnvironment()) {
				LOGGER.warn("DashLoader launched in dev.");
			}

			this.io.setCacheArea(this.metadata.modInfo);
			this.io.setSubCacheArea("bootstrap");
			this.io.addSerializer(RegistryData.class, dashObjects, DashBlockState.class, DashFont.class, DashSprite.class, DashPredicate.class);
			this.io.addSerializer(ImageData.class, dashObjects, DashImage.class);
			this.io.addSerializer(ModelData.class, dashObjects, DashModel.class);
			this.io.addSerializer(IdentifierData.class, dashObjects, DashIdentifierInterface.class);
			this.io.addSerializer(BakedQuadData.class, dashObjects, DashBakedQuad.class);
			this.io.addSerializer(MappingData.class, dashObjects);

			LOGGER.info("Created DashLoader with {}.", classLoader.getClass().getSimpleName());
			LOGGER.info("Initialized DashLoader");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("mc exception bad");
		}
	}

	public static DashDataManager getData() {
		final DashDataManager dataManager = INSTANCE.dataManager;
		if (INSTANCE.dataManager == null) {
			throw new NullPointerException("No dataManager active");
		}
		return dataManager;
	}

	public void reload(List<String> resourcePacks) {
		if (this.shouldReload) {
			this.metadata.setResourcePackHash(resourcePacks);
			this.io.setSubCacheArea(this.metadata.resourcePacks);
			LOGGER.info("Reloading DashLoader. [mod-hash: {}] [resource-hash: {}]", this.metadata.modInfo, this.metadata.resourcePacks);
			if (this.io.cacheExists()) {
				this.setStatus(Status.READ);
				this.loadDashCache();
			} else {
				this.setStatus(Status.WRITE);
			}

			LOGGER.info("Reloaded DashLoader");
			this.shouldReload = false;
		}
	}

	public void requestReload() {
		this.shouldReload = true;
	}

	public void resetDashLoader() {
		this.setStatus(Status.NONE);
	}

	@SuppressWarnings("RedundantTypeArguments")
	public void saveDashCache() {
		this.api.callHook(SaveCacheHook.class, SaveCacheHook::saveCacheStart);
		DashCachingScreen.STATUS = DashCachingScreen.Status.CACHING;
		LOGGER.info("Starting DashLoader Caching");
		try {
			long start = System.currentTimeMillis();

			StepTask main = new StepTask("Creating DashCache", 12);
			this.api.callHook(SaveCacheHook.class, hook -> hook.saveCacheTask(main));

			ProgressHandler.TASK = main;
			this.progress.setCurrentTask("initializing");

			// missing model callback
			this.registry.<BakedModel, DashModel>addCallback(DashModel.class, (rraw, registry) -> {
				final DashDataManager.DashWriteContextData writeContextData = getData().getWriteContextData();
				if (writeContextData.missingModelsWrite.containsKey(rraw)) {
					return writeContextData.missingModelsWrite.get(rraw);
				}
				final DashMissingDashModel value = new DashMissingDashModel();
				writeContextData.missingModelsWrite.put(rraw, value);
				return value;
			});

			this.registry.<Identifier, DashIdentifierInterface>addCallback(DashIdentifierInterface.class, (rraw, registry) -> {
				if (rraw instanceof ModelIdentifier m) {
					return new DashModelIdentifier(m);
				} else {
					return new DashIdentifier(rraw);
				}
			});

			this.api.callHook(SaveCacheHook.class, hook -> hook.saveCacheRegistryInit(this.registry));

			// creation
			RegistryWriter writer = this.registry.createWriter();
			this.api.callHook(SaveCacheHook.class, hook -> hook.saveCacheRegistryWriterInit(writer));

			// mapping
			MappingData mappings = new MappingData();
			this.api.callHook(SaveCacheHook.class, hook -> hook.saveCacheMappingStart(writer, mappings));
			mappings.map(writer, main);
			this.api.callHook(SaveCacheHook.class, hook -> hook.saveCacheMappingEnd(writer, mappings));

			// export
			List<ChunkHolder> holders = new ArrayList<>();
			this.progress.setCurrentTask("export.image");
			main.run(() -> holders.add(new ImageData(writer)));
			this.progress.setCurrentTask("export.model");
			main.run(() -> holders.add(new ModelData(writer)));
			this.progress.setCurrentTask("export.registry");
			main.run(() -> holders.add(new RegistryData(writer)));
			this.progress.setCurrentTask("export.identifier");
			main.run(() -> holders.add(new IdentifierData(writer)));
			this.progress.setCurrentTask("export.quad");
			main.run(() -> holders.add(new BakedQuadData(writer)));
			this.api.callHook(SaveCacheHook.class, hook -> hook.saveCachePopulateHolders(writer, mappings, holders));


			// serialization
			holders.forEach(holder -> main.run(() -> this.io.save(holder, main::setSubTask)));
			main.run(() -> this.io.save(mappings, main::setSubTask));
			this.api.callHook(SaveCacheHook.class, hook -> hook.saveCacheSerialize(writer, mappings, holders));


			LOGGER.info("Created cache in " + TimeUtil.getTimeStringFromStart(start));
			DashCachingScreen.STATUS = DashCachingScreen.Status.DONE;
			this.api.callHook(SaveCacheHook.class, SaveCacheHook::saveCacheEnd);
		} catch (Throwable thr) {
			this.setStatus(Status.WRITE);
			LOGGER.error("Failed caching", thr);
			DashCachingScreen.STATUS = DashCachingScreen.Status.CRASHED;
			this.io.clearCache();
		}
	}

	private void loadDashCache() {
		this.api.callHook(LoadCacheHook.class, LoadCacheHook::loadCacheStart);


		var start = System.currentTimeMillis();
		this.io.setSubCacheArea(this.metadata.resourcePacks);
		LOGGER.info("Starting DashLoader Deserialization");
		try {
			throw new RuntimeException();
			// StepTask task = new StepTask("Loading DashCache", 3);
			//			this.api.callHook(LoadCacheHook.class, (hook) -> hook.loadCacheTask(task));
			//			ProgressHandler.TASK = task;
			//
			//			AtomicReference<MappingData> mappingsReference = new AtomicReference<>();
			//			ChunkHolder[] registryDataObjects = new ChunkHolder[5];
			//
			//			var tempStart = System.currentTimeMillis();
			//			// Deserialize / Decompress all registries and mappings.
			//			List<@Nullable Task> stages = new ArrayList<>();
			//			for (int i = 0; i < 6; i++) {
			//				stages.add(StaticTask.EMPTY);
			//			}
			//			task.run(new StageTask("Deserialization", stages), (subTask) -> {
			//				this.api.callHook(LoadCacheHook.class, LoadCacheHook::loadCacheDeserialization);
			//				this.thread.parallelRunnable(
			//						() -> registryDataObjects[0] = (this.io.load(RegistryData.class, (t) -> stages.set(0, t))),
			//						() -> registryDataObjects[1] = (this.io.load(ImageData.class, (t) -> stages.set(1, t))),
			//						() -> registryDataObjects[2] = (this.io.load(ModelData.class, (t) -> stages.set(2, t))),
			//						() -> registryDataObjects[3] = (this.io.load(IdentifierData.class, (t) -> stages.set(3, t))),
			//						() -> registryDataObjects[4] = (this.io.load(BakedQuadData.class, (t) -> stages.set(4, t))),
			//						() -> mappingsReference.set(this.io.load(MappingData.class, (t) -> stages.set(5, t)))
			//				);
			//			});
			//			EXPORT_READING_TIME = System.currentTimeMillis() - tempStart;
			//
			//			MappingData mappings = mappingsReference.get();
			//			assert mappings != null;
			//
			//			// Initialize systems
			//			LOGGER.info("Creating Registry");
			//			final RegistryReader reader = this.registry.createReader(registryDataObjects);
			//			this.api.callHook(LoadCacheHook.class, (hook) -> hook.loadCacheRegistryInit(reader, this.dataManager, mappings));
			//
			//			tempStart = System.currentTimeMillis();
			//			LOGGER.info("Exporting Mappings");
			//			task.run(() -> {
			//				reader.export(task::setSubTask);
			//				this.api.callHook(LoadCacheHook.class, (hook) -> hook.loadCacheExported(reader, this.dataManager, mappings));
			//			});
			//			EXPORT_EXPORTING_TIME = System.currentTimeMillis() - tempStart;
			//
			//
			//			tempStart = System.currentTimeMillis();
			//			LOGGER.info("Loading Mappings");
			//			task.run(() -> {
			//				mappings.export(reader, this.dataManager, task::setSubTask);
			//				this.api.callHook(LoadCacheHook.class, (hook) -> hook.loadCacheMapped(reader, this.dataManager, mappings));
			//			});
			//			EXPORT_LOADING_TIME = System.currentTimeMillis() - tempStart;
			//
			//
			//			EXPORT_TIME = System.currentTimeMillis() - start;
			//			LOGGER.info("Loaded DashLoader in {}", EXPORT_TIME);
			//			this.api.callHook(LoadCacheHook.class, LoadCacheHook::loadCacheEnd);
		} catch (Exception e) {
			LOGGER.error("Summoned CrashLoader in {}", TimeUtil.getTimeStringFromStart(start), e);
			this.setStatus(Status.WRITE);
			this.io.clearCache();
		}
	}

	private static List<DashObjectClass<?, ?>> parseDashObjects(Collection<Class<?>> dashClasses) {
		var out = new ArrayList<DashObjectClass<?, ?>>();
		for (Class<?> dashClass : dashClasses) {
			out.add(new DashObjectClass(dashClass));
		}
		return Collections.unmodifiableList(out);
	}

	private void setStatus(Status status) {
		LOGGER.info("\u001B[46m\u001B[30m DashLoader Status change {}\n\u001B[0m", status);
		STATUS = status;
		switch (status) {
			case NONE -> this.dataManager = null;
			case READ -> this.dataManager = new DashDataManager(new DashDataManager.DashReadContextData());
			case WRITE -> this.dataManager = new DashDataManager(new DashDataManager.DashWriteContextData());
		}
	}

	public static boolean isWrite() {
		return STATUS == Status.WRITE;
	}

	public static boolean isRead() {
		return STATUS == Status.READ;
	}

	public static Status getStatus() {
		return STATUS;
	}


	public enum Status {
		NONE,
		READ,
		WRITE,
	}

	public static class DashMetadata {
		public String modInfo;
		public String resourcePacks;

		public void setModHash(FabricLoader loader) {
			ArrayList<ModMetadata> versions = new ArrayList<>();
			for (ModContainer mod : loader.getAllMods()) {
				ModMetadata metadata = mod.getMetadata();
				versions.add(metadata);
			}

			versions.sort(Comparator.comparing(ModMetadata::getId));

			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < versions.size(); i++) {
				ModMetadata metadata = versions.get(i);
				stringBuilder.append(i).append("$").append(metadata.getId()).append('&').append(metadata.getVersion().getFriendlyString());
			}

			this.modInfo = DigestUtils.md5Hex(stringBuilder.toString()).toUpperCase();
		}

		public void setResourcePackHash(List<String> resourcePacks) {
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < resourcePacks.size(); i++) {
				String resourcePack = resourcePacks.get(i);
				stringBuilder.append(i).append("$").append(resourcePack);
			}

			this.resourcePacks = DigestUtils.md5Hex(stringBuilder.toString()).toUpperCase();
		}
	}

}
