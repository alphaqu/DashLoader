package dev.quantumfusion.dashloader.def;

import dev.quantumfusion.dashloader.core.DashLoaderCore;
import dev.quantumfusion.dashloader.core.io.IOHandler;
import dev.quantumfusion.dashloader.core.progress.ProgressHandler;
import dev.quantumfusion.dashloader.core.registry.ChunkHolder;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.api.DashLoaderAPI;
import dev.quantumfusion.dashloader.def.api.hook.LoadCacheHook;
import dev.quantumfusion.dashloader.def.api.hook.SaveCacheHook;
import dev.quantumfusion.dashloader.def.client.DashCachingScreen;
import dev.quantumfusion.dashloader.def.corehook.*;
import dev.quantumfusion.dashloader.def.data.DashIdentifier;
import dev.quantumfusion.dashloader.def.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.def.data.DashModelIdentifier;
import dev.quantumfusion.dashloader.def.data.blockstate.DashBlockState;
import dev.quantumfusion.dashloader.def.data.font.DashFont;
import dev.quantumfusion.dashloader.def.data.image.DashImage;
import dev.quantumfusion.dashloader.def.data.image.DashSprite;
import dev.quantumfusion.dashloader.def.data.model.DashModel;
import dev.quantumfusion.dashloader.def.data.model.components.DashBakedQuad;
import dev.quantumfusion.dashloader.def.data.model.predicates.DashPredicate;
import dev.quantumfusion.dashloader.def.fallback.model.DashMissingDashModel;
import dev.quantumfusion.dashloader.def.util.TimeUtil;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class DashLoader {
	public static final Logger LOGGER = LogManager.getLogger("DashLoader");
	public static final String VERSION = FabricLoader.getInstance()
			.getModContainer("dashloader")
			.orElseThrow(() -> new IllegalStateException("DashLoader not found... apparently! WTF?"))
			.getMetadata()
			.getVersion()
			.getFriendlyString();
	public static final Path DASH_CONFIG_FOLDER = FabricLoader.getInstance().getConfigDir().normalize();
	public static final Path DASH_CACHE_FOLDER = Path.of("./dashloader-cache/");
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
		metadata.setModHash(FabricLoader.getInstance());
		final var dlcLogger = LogManager.getLogger("dl-core");
		DashLoaderCore.initialize(DASH_CACHE_FOLDER.resolve("mods-" + metadata.modInfo + "/"), DASH_CONFIG_FOLDER.resolve("dashloader.json"), new DashLoaderCore.Printer(dlcLogger::info, dlcLogger::warn, dlcLogger::error));
		DashLoaderCore.CORE.prepareCore();
	}

	private void initInternal(ClassLoader classLoader) {
		try {
			api.initAPI();
			DashLoaderCore.CORE.launchCore(api.dashObjects);
			DashLoaderCore.CONFIG.reloadConfig();

			final FabricLoader instance = FabricLoader.getInstance();
			if (instance.isDevelopmentEnvironment())
				LOGGER.warn("DashLoader launched in dev.");

			final IOHandler io = DashLoaderCore.IO;
			io.setCacheArea(metadata.modInfo);
			io.setSubCacheArea("bootstrap");
			io.addSerializer(RegistryData.class, DashBlockState.class, DashFont.class, DashSprite.class, DashPredicate.class);
			io.addSerializer(ImageData.class, DashImage.class);
			io.addSerializer(ModelData.class, DashModel.class);
			io.addSerializer(IdentifierData.class, DashIdentifierInterface.class);
			io.addSerializer(BakedQuadData.class, DashBakedQuad.class);
			io.addSerializer(MappingData.class);

			LOGGER.info("Created DashLoader with {}.", classLoader.getClass().getSimpleName());
			LOGGER.info("Initialized DashLoader");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("mc exception bad");
		}
	}

	public static DashDataManager getData() {
		final DashDataManager dataManager = INSTANCE.dataManager;
		if (INSTANCE.dataManager == null)
			throw new NullPointerException("No dataManager active");
		return dataManager;
	}

	public void reload(List<String> resourcePacks) {
		if (shouldReload) {
			metadata.setResourcePackHash(resourcePacks);
			DashLoaderCore.IO.setSubCacheArea(metadata.resourcePacks);
			LOGGER.info("Reloading DashLoader. [mod-hash: {}] [resource-hash: {}]", metadata.modInfo, metadata.resourcePacks);
			if (DashLoaderCore.IO.cacheExists()) {
				this.setStatus(Status.READ);
				loadDashCache();
			} else {
				this.setStatus(Status.WRITE);
			}

			LOGGER.info("Reloaded DashLoader");
			shouldReload = false;
		}
	}

	public void requestReload() {
		shouldReload = true;
	}

	public void resetDashLoader() {
		this.setStatus(Status.NONE);
	}

	@SuppressWarnings("RedundantTypeArguments")
	public void saveDashCache() {
		api.callHook(SaveCacheHook.class, SaveCacheHook::saveCacheStart);
		DashCachingScreen.STATUS = DashCachingScreen.Status.CACHING;
		LOGGER.info("Starting DashLoader Caching");
		try {
			long start = System.currentTimeMillis();

			final ProgressHandler progress = DashLoaderCore.PROGRESS;
			StepTask main = new StepTask("Creating DashCache", 12);
			api.callHook(SaveCacheHook.class, hook -> hook.saveCacheTask(main));

			ProgressHandler.TASK = main;
			progress.setCurrentTask("initializing");

			// missing model callback
			DashLoaderCore.REGISTRY.<BakedModel, DashModel>addCallback(DashModel.class, (rraw, registry) -> {
				final DashDataManager.DashWriteContextData writeContextData = getData().getWriteContextData();
				if (writeContextData.missingModelsWrite.containsKey(rraw)) {
					return writeContextData.missingModelsWrite.get(rraw);
				}
				final DashMissingDashModel value = new DashMissingDashModel();
				writeContextData.missingModelsWrite.put(rraw, value);
				return value;
			});

			DashLoaderCore.REGISTRY.<Identifier, DashIdentifierInterface>addCallback(DashIdentifierInterface.class, (rraw, registry) -> {
				if (rraw instanceof ModelIdentifier m) return new DashModelIdentifier(m);
				else return new DashIdentifier(rraw);
			});

			api.callHook(SaveCacheHook.class, hook -> hook.saveCacheRegistryInit(DashLoaderCore.REGISTRY));

			// creation
			RegistryWriter writer = DashLoaderCore.REGISTRY.createWriter();
			api.callHook(SaveCacheHook.class, hook -> hook.saveCacheRegistryWriterInit(writer));

			// mapping
			MappingData mappings = new MappingData();
			api.callHook(SaveCacheHook.class, hook -> hook.saveCacheMappingStart(writer, mappings));
			mappings.map(writer, main);
			api.callHook(SaveCacheHook.class, hook -> hook.saveCacheMappingEnd(writer, mappings));

			// export
			List<ChunkHolder> holders = new ArrayList<>();
			progress.setCurrentTask("export.image");
			main.run(() -> holders.add(new ImageData(writer)));
			progress.setCurrentTask("export.model");
			main.run(() -> holders.add(new ModelData(writer)));
			progress.setCurrentTask("export.registry");
			main.run(() -> holders.add(new RegistryData(writer)));
			progress.setCurrentTask("export.identifier");
			main.run(() -> holders.add(new IdentifierData(writer)));
			progress.setCurrentTask("export.quad");
			main.run(() -> holders.add(new BakedQuadData(writer)));
			api.callHook(SaveCacheHook.class, hook -> hook.saveCachePopulateHolders(writer, mappings, holders));


			final IOHandler io = DashLoaderCore.IO;
			// serialization
			holders.forEach(holder -> main.run(() -> io.save(holder, main::setSubTask)));
			main.run(() -> io.save(mappings, main::setSubTask));
			api.callHook(SaveCacheHook.class, hook -> hook.saveCacheSerialize(writer, mappings, holders));


			LOGGER.info("Created cache in " + TimeUtil.getTimeStringFromStart(start));
			DashCachingScreen.STATUS = DashCachingScreen.Status.DONE;
			api.callHook(SaveCacheHook.class, SaveCacheHook::saveCacheEnd);
		} catch (Throwable thr) {
			this.setStatus(Status.NONE);
			LOGGER.error("Failed caching", thr);
			DashCachingScreen.STATUS = DashCachingScreen.Status.CRASHED;
			DashLoaderCore.IO.clearCache();
		}
	}

	private void loadDashCache() {
		api.callHook(LoadCacheHook.class, LoadCacheHook::loadCacheStart);


		var start = System.currentTimeMillis();
		final IOHandler io = DashLoaderCore.IO;
		io.setSubCacheArea(metadata.resourcePacks);
		LOGGER.info("Starting DashLoader Deserialization");
		try {
			StepTask task = new StepTask("Loading DashCache", 3);
			api.callHook(LoadCacheHook.class, (hook) -> hook.loadCacheTask(task));
			ProgressHandler.TASK = task;

			AtomicReference<MappingData> mappingsReference = new AtomicReference<>();
			ChunkHolder[] registryDataObjects = new ChunkHolder[5];

			var tempStart = System.currentTimeMillis();
			// Deserialize / Decompress all registries and mappings.
			List<@Nullable Task> stages = new ArrayList<>();
			for (int i = 0; i < 6; i++) {
				stages.add(StaticTask.EMPTY);
			}
			task.run(new StageTask("Deserialization", stages), (subTask) -> {
				api.callHook(LoadCacheHook.class, LoadCacheHook::loadCacheDeserialization);
				DashLoaderCore.THREAD.parallelRunnable(
						() -> registryDataObjects[0] = (io.load(RegistryData.class, (t) -> stages.set(0, t))),
						() -> registryDataObjects[1] = (io.load(ImageData.class, (t) -> stages.set(1, t))),
						() -> registryDataObjects[2] = (io.load(ModelData.class, (t) -> stages.set(2, t))),
						() -> registryDataObjects[3] = (io.load(IdentifierData.class, (t) -> stages.set(3, t))),
						() -> registryDataObjects[4] = (io.load(BakedQuadData.class, (t) -> stages.set(4, t))),
						() -> mappingsReference.set(io.load(MappingData.class, (t) -> stages.set(5, t)))
				);
			});
			EXPORT_READING_TIME = System.currentTimeMillis() - tempStart;

			MappingData mappings = mappingsReference.get();
			assert mappings != null;

			// Initialize systems
			LOGGER.info("Creating Registry");
			final RegistryReader reader = DashLoaderCore.REGISTRY.createReader(registryDataObjects);
			api.callHook(LoadCacheHook.class, (hook) -> hook.loadCacheRegistryInit(reader, dataManager, mappings));

			tempStart = System.currentTimeMillis();
			LOGGER.info("Exporting Mappings");
			task.run(() -> {
				reader.export(task::setSubTask);
				api.callHook(LoadCacheHook.class, (hook) -> hook.loadCacheExported(reader, dataManager, mappings));
			});
			EXPORT_EXPORTING_TIME = System.currentTimeMillis() - tempStart;


			tempStart = System.currentTimeMillis();
			LOGGER.info("Loading Mappings");
			task.run(() -> {
				mappings.export(reader, this.dataManager, task::setSubTask);
				api.callHook(LoadCacheHook.class, (hook) -> hook.loadCacheMapped(reader, dataManager, mappings));
			});
			EXPORT_LOADING_TIME = System.currentTimeMillis() - tempStart;


			EXPORT_TIME = System.currentTimeMillis() - start;
			LOGGER.info("Loaded DashLoader in {}", EXPORT_TIME);
			api.callHook(LoadCacheHook.class, LoadCacheHook::loadCacheEnd);
		} catch (Exception e) {
			LOGGER.error("Summoned CrashLoader in {}", TimeUtil.getTimeStringFromStart(start), e);
			this.setStatus(Status.NONE);
			DashLoaderCore.IO.clearCache();
		}
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
