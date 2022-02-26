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
import dev.quantumfusion.dashloader.def.util.mixins.MixinThings;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
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
			var api = new DashLoaderAPI();
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
		LOGGER.info("Starting DashLoader Caching");
		try {
			long start = System.currentTimeMillis();

			final ProgressHandler progress = DashLoaderCore.PROGRESS;
			CountTask main = new CountTask(12);
			progress.setTask(main);
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
			// creation
			RegistryWriter writer = DashLoaderCore.REGISTRY.createWriter();

			// mapping
			MappingData mappings = new MappingData();
			mappings.map(writer, main);
			main.completedTask();

			// export
			List<ChunkHolder> holders = new ArrayList<>();
			progress.setCurrentTask("export.image");
			main.task(() -> holders.add(new ImageData(writer)));
			progress.setCurrentTask("export.model");
			main.task(() -> holders.add(new ModelData(writer)));
			progress.setCurrentTask("export.registry");
			main.task(() -> holders.add(new RegistryData(writer)));
			progress.setCurrentTask("export.identifier");
			main.task(() -> holders.add(new IdentifierData(writer)));
			progress.setCurrentTask("export.quad");
			main.task(() -> holders.add(new BakedQuadData(writer)));

			final IOHandler io = DashLoaderCore.IO;
			// serialization
			holders.forEach(holder -> main.task(() -> io.save(holder)));
			main.task(() -> io.save(mappings));

			DashCachingScreen.CACHING_COMPLETE = true;
			LOGGER.info("Created cache in " + TimeUtil.getTimeStringFromStart(start));
		} catch (Throwable thr) {
			this.setStatus(Status.NONE);
			LOGGER.error("Failed caching", thr);
		}
	}

	public void loadDashCache() {
		var start = System.currentTimeMillis();
		final IOHandler io = DashLoaderCore.IO;
		io.setSubCacheArea(metadata.resourcePacks);
		LOGGER.info("Starting DashLoader Deserialization");
		try {
			AtomicReference<MappingData> mappingsReference = new AtomicReference<>();
			ChunkHolder[] registryDataObjects = new ChunkHolder[5];


			var start2 = System.currentTimeMillis();
			DashLoaderCore.THREAD.parallelRunnable(
					() -> registryDataObjects[0] = (io.load(RegistryData.class)),
					() -> registryDataObjects[1] = (io.load(ImageData.class)),
					() -> registryDataObjects[2] = (io.load(ModelData.class)),
					() -> registryDataObjects[3] = (io.load(IdentifierData.class)),
					() -> registryDataObjects[4] = (io.load(BakedQuadData.class)),
					() -> mappingsReference.set(io.load(MappingData.class))
			);
			EXPORT_READING_TIME = System.currentTimeMillis() - start2;

			MappingData mappings = mappingsReference.get();
			assert mappings != null;

			LOGGER.info("Creating Registry");
			final RegistryReader reader = DashLoaderCore.REGISTRY.createReader(registryDataObjects);

			this.dataManager = new DashDataManager(new DashDataManager.DashReadContextData());

			start2 = System.currentTimeMillis();
			LOGGER.info("Exporting Mappings");
			reader.export();
			EXPORT_EXPORTING_TIME = System.currentTimeMillis() - start2;

			start2 = System.currentTimeMillis();
			LOGGER.info("Loading Mappings");
			mappings.export(reader, this.dataManager);
			EXPORT_LOADING_TIME = System.currentTimeMillis() - start2;


			EXPORT_TIME = System.currentTimeMillis() - start;
			LOGGER.info("Loaded DashLoader in {}", EXPORT_TIME);
		} catch (Exception e) {
			LOGGER.error("Summoned CrashLoader in {}", TimeUtil.getTimeStringFromStart(start), e);
			this.setStatus(Status.NONE);
			if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
				// TODO REMOVE FILES IF IT CRASHED
			}
		}
	}

	public void complete(MinecraftClient client) {
		LOGGER.info("┏ DashLoader Profiler Times.");
		if (EXPORT_TIME != -1) {
			LOGGER.info("┠──┬ {} DashLoader Load", TimeUtil.getTimeString(EXPORT_TIME));
			LOGGER.info("┃  ├── {} File Reading", TimeUtil.getTimeString(EXPORT_READING_TIME));
			LOGGER.info("┃  ├── {} Asset Exporting", TimeUtil.getTimeString(EXPORT_EXPORTING_TIME));
			LOGGER.info("┃  └── {} Asset Loading", TimeUtil.getTimeString(EXPORT_LOADING_TIME));
			EXPORT_TIME = -1;
		}
		LOGGER.info("┠── {} Minecraft Client Reload", TimeUtil.getTimeStringFromStart(DashLoader.RELOAD_START));
		LOGGER.info("┠── {} Minecraft Bootstrap", TimeUtil.getTimeString(MixinThings.BOOTSTRAP_END - MixinThings.BOOTSTRAP_START));
		LOGGER.info("┠── {} Total Loading", TimeUtil.getTimeString(ManagementFactory.getRuntimeMXBean().getUptime()));

		if (DashLoader.isWrite()) {
			// Yes this is bad. But it makes us not require Fabric API
			var langCode = MinecraftClient.getInstance().getLanguageManager().getLanguage().getCode();
			var stream = this.getClass().getClassLoader().getResourceAsStream("assets/dashloader/lang/" + langCode + ".json");
			var map = new HashMap<String, String>();
			if (stream != null) {
				Language.load(stream, map::put);
			}
			DashLoaderCore.PROGRESS.setTranslations(map);
			client.currentScreen = new DashCachingScreen(client.currentScreen);
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
