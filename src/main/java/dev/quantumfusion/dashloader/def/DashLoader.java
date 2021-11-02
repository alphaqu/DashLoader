package dev.quantumfusion.dashloader.def;

import dev.quantumfusion.dashloader.core.DashLoaderCore;
import dev.quantumfusion.dashloader.core.registry.ChunkDataHolder;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.core.registry.WriteFailCallback;
import dev.quantumfusion.dashloader.core.util.DashThreading;
import dev.quantumfusion.dashloader.def.api.DashLoaderAPI;
import dev.quantumfusion.dashloader.def.client.DashCachingScreen;
import dev.quantumfusion.dashloader.def.corehook.ImageData;
import dev.quantumfusion.dashloader.def.corehook.MappingData;
import dev.quantumfusion.dashloader.def.corehook.ModelData;
import dev.quantumfusion.dashloader.def.corehook.RegistryData;
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
import dev.quantumfusion.dashloader.def.fallback.MissingDashModel;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static dev.quantumfusion.dashloader.core.ui.DashLoaderProgress.PROGRESS;


public class DashLoader {
	public static final Logger LOGGER = LogManager.getLogger("DashLoader");
	public static final DashLoader INSTANCE = new DashLoader();
	public static final String VERSION = FabricLoader.getInstance()
			.getModContainer("dashloader")
			.orElseThrow(() -> new IllegalStateException("DashLoader not found... apparently! WTF?"))
			.getMetadata()
			.getVersion()
			.getFriendlyString();
	private boolean shouldReload = true;
	private final DashMetadata metadata = new DashMetadata();
	private final DashLoaderCore core;
	private DashDataManager dataManager;
	private Status status;

	public static void init() {
		LOGGER.info("Initializing DashLoader " + VERSION + ".");
		INSTANCE.initialize(Thread.currentThread().getContextClassLoader());
	}

	private DashLoader() {
		var api = new DashLoaderAPI(this);
		api.initAPI();
		final FabricLoader instance = FabricLoader.getInstance();
		metadata.setModHash(instance);
		final Path dashFolder = instance.getConfigDir().normalize().resolve("quantumfusion/dashloader/");
		this.core = new DashLoaderCore(dashFolder.resolve("mods-" + metadata.modInfo + "/"), api.dashObjects.toArray(Class[]::new));
	}

	private void initialize(ClassLoader classLoader) {
		try {
			final FabricLoader instance = FabricLoader.getInstance();
			if (instance.isDevelopmentEnvironment())
				LOGGER.warn("DashLoader launched in dev.");

			core.setCurrentSubcache("null");
			core.prepareSerializer(RegistryData.class, DashBlockState.class, DashFont.class, DashIdentifierInterface.class, DashSprite.class, DashPredicate.class, DashBakedQuad.class);
			core.prepareSerializer(ImageData.class, DashImage.class);
			core.prepareSerializer(ModelData.class, DashModel.class);
			core.prepareSerializer(MappingData.class);

			status = Status.WRITE;
			dataManager = new DashDataManager(new DashDataManager.DashWriteContextData());
			LOGGER.info("Created DashLoader with {} classloader.", classLoader.getClass().getSimpleName());
			LOGGER.info("Initialized DashLoader");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("mc exception bad");
		}
	}

	public static DashDataManager getData() {
		final DashDataManager dataManager = INSTANCE.dataManager;
		if (dataManager == null)
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
			core.setCurrentSubcache(metadata.resourcePacks);
			LOGGER.info("Reloading DashLoader. [mod-hash: {}] [resource-hash: {}]", metadata.modInfo, metadata.resourcePacks);
			if (core.isCacheMissing()) cacheEmpty();
			else loadDashCache();


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

	public void saveDashCache() {
		PROGRESS.reset();
		PROGRESS.setTotalTasks(5);
		Map<Class<?>, WriteFailCallback<?, ?>> callbacks = new HashMap<>();

		// missing model callback
		callbacks.put(DashModel.class, (WriteFailCallback<BakedModel, DashModel>) (rraw, registry) -> {
			final DashDataManager.DashWriteContextData writeContextData = getData().getWriteContextData();
			if (writeContextData.missingModelsWrite.containsKey(rraw)) {
				return writeContextData.missingModelsWrite.get(rraw);
			}
			final MissingDashModel value = new MissingDashModel();
			writeContextData.missingModelsWrite.put(rraw, value);
			return value;
		});

		callbacks.put(DashIdentifierInterface.class, (WriteFailCallback<Identifier, DashIdentifierInterface>) (rraw, registry) -> {
			if (rraw instanceof ModelIdentifier m) return new DashModelIdentifier(m);
			else return new DashIdentifier(rraw);
		});

		// creation
		DashRegistryWriter writer = core.createWriter(callbacks);
		PROGRESS.completedTask();

		// mapping
		MappingData mappings = new MappingData();
		mappings.map(writer);
		PROGRESS.completedTask();

		// export
		final ImageData images = new ImageData(writer);
		PROGRESS.completedTask();
		final ModelData models = new ModelData(writer);
		final RegistryData registrydata = new RegistryData(writer);
		PROGRESS.completedTask();

		// serialization
		core.save(images);
		core.save(models);
		core.save(registrydata);
		core.save(mappings);
		PROGRESS.completedTask();
		DashCachingScreen.CACHING_COMPLETE = true;
		LOGGER.info("Created cache in " + "TODO" + "s");
	}

	public void loadDashCache() {
		core.setCurrentSubcache(metadata.resourcePacks);
		LOGGER.info("Starting DashLoader Deserialization");
		try {
			AtomicReference<MappingData> mappingsReference = new AtomicReference<>();
			ChunkDataHolder[] registryDataObjects = new ChunkDataHolder[3];


			DashThreading.run(
					() -> registryDataObjects[0] = (core.load(RegistryData.class)),
					() -> registryDataObjects[1] = (core.load(ImageData.class)),
					() -> registryDataObjects[2] = (core.load(ModelData.class)),
					() -> mappingsReference.set(core.load(MappingData.class))
			);

			MappingData mappings = mappingsReference.get();
			assert mappings != null;

			LOGGER.info("      Creating Registry");
			final DashRegistryReader reader = core.createReader(registryDataObjects);

			status = Status.READ;
			this.dataManager = new DashDataManager(new DashDataManager.DashReadContextData());

			LOGGER.info("      Loading Mappings");
			mappings.export(reader, this.dataManager);


			LOGGER.info("    Loaded DashLoader");
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
			this.modInfo = Long.toHexString(modInfoData + 0x69);
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
