package dev.quantumfusion.dashloader.def;

import dev.quantumfusion.dashloader.core.DashLoaderCore;
import dev.quantumfusion.dashloader.core.registry.ChunkDataHolder;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.core.ui.DashLoaderProgress;
import dev.quantumfusion.dashloader.def.api.DashLoaderAPI;
import dev.quantumfusion.dashloader.def.client.DashCachingScreen;
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
import dev.quantumfusion.dashloader.def.data.model.DashModel;
import dev.quantumfusion.dashloader.def.data.model.components.DashBakedQuad;
import dev.quantumfusion.dashloader.def.data.model.predicates.DashPredicate;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import static dev.quantumfusion.dashloader.core.ui.DashLoaderProgress.PROGRESS;


public class DashLoader {
	public static final Logger LOGGER = LogManager.getLogger("DashLoader");
	public static final String VERSION = FabricLoader.getInstance()
			.getModContainer("dashloader")
			.orElseThrow(() -> new IllegalStateException("DashLoader not found... apparently! WTF?"))
			.getMetadata()
			.getVersion()
			.getFriendlyString();

	public static final Path MAIN_PATH = FabricLoader.getInstance().getConfigDir().normalize().resolve("quantumfusion/dashloader/");
	public static final VanillaData VANILLA_DATA = new VanillaData();
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
		PROGRESS.reset();
		PROGRESS.setTotalTasks(5);
		DashRegistryWriter writer = core.createWriter();
		PROGRESS.completedTask();
		mappings = new MappingData(writer);
		mappings.map();
		PROGRESS.completedTask();
		core.save(new ImageData(writer));
		PROGRESS.completedTask();
		core.save(new ModelData(writer));
		PROGRESS.completedTask();
		core.save(new RegistryData(writer));
		core.save(mappings);
		PROGRESS.completedTask();
		DashCachingScreen.exit = true;
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
