package dev.quantumfusion.dashloader.def;

import dev.quantumfusion.dashloader.core.DashLoaderCore;
import dev.quantumfusion.dashloader.core.io.IOHandler;
import dev.quantumfusion.dashloader.core.progress.ProgressHandler;
import dev.quantumfusion.dashloader.core.progress.task.CountTask;
import dev.quantumfusion.dashloader.core.registry.ChunkHolder;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.api.DashLoaderAPI;
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
import dev.quantumfusion.dashloader.def.fallback.DashMissingDashModel;
import dev.quantumfusion.dashloader.def.util.TimeUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
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
	public static long EXPORT_START = 0;
	public static long EXPORT_END = 0;
	private boolean shouldReload = true;
	private final DashMetadata metadata = new DashMetadata();
	private DashLoaderCore core;
	private DashDataManager dataManager;
	private Status status;

	public static void init() {
		LOGGER.info("Initializing DashLoader " + VERSION + ".");
		INSTANCE.initialize(Thread.currentThread().getContextClassLoader());
	}

	private DashLoader() {

	}

	private void initialize(ClassLoader classLoader) {
		try {
			var api = new DashLoaderAPI();
			api.initAPI();
			metadata.setModHash(FabricLoader.getInstance());
			final Logger dlcLogger = LogManager.getLogger("DashLoaderCore");
			DashLoaderCore.initialize(DASH_CACHE_FOLDER.resolve("mods-" + metadata.modInfo + "/"), DASH_CONFIG_FOLDER.resolve("dashloader.json"), api.dashObjects, dlcLogger::info);

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

			status = Status.WRITE;
			dataManager = new DashDataManager(new DashDataManager.DashWriteContextData());
			LOGGER.info("Created DashLoader with {}.", classLoader.getClass().getSimpleName());
			LOGGER.info("Initialized DashLoader");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("mc exception bad");
		}
	}

	public static boolean dataManagerActive() {
		return INSTANCE.dataManager != null;
	}

	public static DashDataManager getData() {
		final DashDataManager dataManager = INSTANCE.dataManager;
		if (!dataManagerActive())
			throw new NullPointerException("No dataManager active");
		return dataManager;
	}

	public static boolean isWrite() {
		return INSTANCE.status != Status.READ;
	}

	public static boolean isRead() {
		return INSTANCE.status == Status.READ;
	}

	public void requestReload() {
		shouldReload = true;
	}

	public void reload(List<String> resourcePacks) {
		if (shouldReload) {
			metadata.setResourcePackHash(resourcePacks);
			DashLoaderCore.IO.setSubCacheArea(metadata.resourcePacks);
			LOGGER.info("Reloading DashLoader. [mod-hash: {}] [resource-hash: {}]", metadata.modInfo, metadata.resourcePacks);
			if (DashLoaderCore.IO.cacheExists()) loadDashCache();
			else cacheEmpty();

			LOGGER.info("Reloaded DashLoader");
			shouldReload = false;
		}
	}

	public void reloadComplete() {
		LOGGER.info("Reload complete");
		this.dataManager = null;
	}

	private void cacheEmpty() {
		this.status = Status.WRITE;
		this.dataManager = new DashDataManager(new DashDataManager.DashWriteContextData());
	}

	@SuppressWarnings("RedundantTypeArguments")
	public void saveDashCache() {
		long start = System.currentTimeMillis();

		final ProgressHandler progress = DashLoaderCore.PROGRESS;
		CountTask main = new CountTask(12);
		progress.setTask(main);
		progress.setCurrentTask("Initializing");

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
		// creation
		RegistryWriter writer = DashLoaderCore.REGISTRY.createWriter();

		// mapping
		MappingData mappings = new MappingData();
		mappings.map(writer, main);
		main.completedTask();

		// export
		List<ChunkHolder> holders = new ArrayList<>();

		progress.setCurrentTask("Exporting Images");
		main.task(() -> holders.add(new ImageData(writer)));
		progress.setCurrentTask("Exporting Models");
		main.task(() -> holders.add(new ModelData(writer)));
		progress.setCurrentTask("Exporting Registry");
		main.task(() -> holders.add(new RegistryData(writer)));
		progress.setCurrentTask("Exporting Identifiers");
		main.task(() -> holders.add(new IdentifierData(writer)));
		progress.setCurrentTask("Exporting BakedQuads");
		main.task(() -> holders.add(new BakedQuadData(writer)));

		final IOHandler io = DashLoaderCore.IO;
		// serialization
		holders.forEach(holder -> main.task(() -> io.save(holder)));
		main.task(() -> io.save(mappings));

		DashCachingScreen.CACHING_COMPLETE = true;
		LOGGER.info("Created cache in " + TimeUtil.getTimeStringFromStart(start));
	}

	public void loadDashCache() {
		EXPORT_START = System.currentTimeMillis();
		final IOHandler io = DashLoaderCore.IO;
		io.setSubCacheArea(metadata.resourcePacks);
		LOGGER.info("Starting DashLoader Deserialization");
		try {
			AtomicReference<MappingData> mappingsReference = new AtomicReference<>();
			ChunkHolder[] registryDataObjects = new ChunkHolder[5];


			DashLoaderCore.THREAD.parallelRunnable(
					() -> registryDataObjects[0] = (io.load(RegistryData.class)),
					() -> registryDataObjects[1] = (io.load(ImageData.class)),
					() -> registryDataObjects[2] = (io.load(ModelData.class)),
					() -> registryDataObjects[3] = (io.load(IdentifierData.class)),
					() -> registryDataObjects[4] = (io.load(BakedQuadData.class)),
					() -> mappingsReference.set(io.load(MappingData.class))
			);

			MappingData mappings = mappingsReference.get();
			assert mappings != null;

			LOGGER.info("Creating Registry");
			final RegistryReader reader = DashLoaderCore.REGISTRY.createReader(registryDataObjects);

			status = Status.READ;
			this.dataManager = new DashDataManager(new DashDataManager.DashReadContextData());

			LOGGER.info("Exporting Mappings");
			reader.export();

			LOGGER.info("Loading Mappings");
			mappings.export(reader, this.dataManager);

			EXPORT_END = System.currentTimeMillis();
			LOGGER.info("Loaded DashLoader in {}", TimeUtil.getTimeString(EXPORT_END - EXPORT_START));
		} catch (Exception e) {
			LOGGER.error("Summoned CrashLoader in {}", TimeUtil.getTimeStringFromStart(EXPORT_START), e);
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
		READ,
		CRASHLOADER,
		WRITE
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
			this.modInfo = Long.toHexString(modInfoData + 0x69).toUpperCase();
		}

		public void setResourcePackHash(List<String> resourcePacks) {
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < resourcePacks.size(); i++) {
				String resourcePack = resourcePacks.get(i);
				stringBuilder.append(i).append(". ").append(resourcePack);
			}

			this.resourcePacks = DigestUtils.md5Hex(stringBuilder.toString()).toUpperCase();
		}
	}

}
