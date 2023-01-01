package dev.quantumfusion.dashloader;

import dev.quantumfusion.dashloader.api.APIHandler;
import dev.quantumfusion.dashloader.api.entrypoint.DashEntrypoint;
import dev.quantumfusion.dashloader.client.DashToast;
import dev.quantumfusion.dashloader.io.MappingSerializer;
import dev.quantumfusion.dashloader.io.data.CacheInfo;
import dev.quantumfusion.dashloader.io.RegistrySerializer;
import dev.quantumfusion.dashloader.io.Serializer;
import dev.quantumfusion.dashloader.registry.RegistryFactory;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.data.ChunkFactory;
import dev.quantumfusion.dashloader.registry.data.StageData;
import dev.quantumfusion.dashloader.registry.factory.MissingHandler;
import dev.quantumfusion.dashloader.util.TimeUtil;
import dev.quantumfusion.taski.builtin.StepTask;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class DashLoader {
	private static final String METADATA_FILE_NAME = "metadata.bin";
	private static final String VERSION = FabricLoader.getInstance()
			.getModContainer("dashloader")
			.orElseThrow(() -> new IllegalStateException("DashLoader not found... apparently! WTF?"))
			.getMetadata()
			.getVersion()
			.getFriendlyString();
	public static final Logger LOG = LogManager.getLogger("DashLoader");
	public static final DashLoader INSTANCE = new DashLoader();

	private boolean shouldReload = true;
	private Status status = Status.NONE;
	private final DashMetadata metadata = new DashMetadata();
	private final Path cacheDir = Path.of("./dashloader-cache/");
	// Serializers
	private final RegistrySerializer registrySerializer;
	private final MappingSerializer mappingsSerializer;
	private final Serializer<CacheInfo> metadataSerializer;

	// Initializes the static singleton
	@SuppressWarnings("EmptyMethod")
	public static void bootstrap() {
	}

	private DashLoader() {
		LOG.info("Initializing DashLoader " + VERSION + ".");
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			LOG.warn("DashLoader launched in dev.");
		}

		this.metadata.setModHash(FabricLoader.getInstance());
		Path cacheAreaDir = this.getCacheAreaDir();
		// Initialize serializers
		this.registrySerializer = new RegistrySerializer(cacheAreaDir, APIHandler.INSTANCE.getDashObjects());
		this.mappingsSerializer = new MappingSerializer(cacheAreaDir, APIHandler.INSTANCE.getCacheHandlers());
		this.metadataSerializer = new Serializer<>(cacheAreaDir, CacheInfo.class);
	}
	public void reload(List<String> resourcePacks) {
		if (this.shouldReload) {
			this.metadata.setResourcePackHash(resourcePacks);
			LOG.info("Reloading DashLoader. [mod-hash: {}] [resource-hash: {}]", this.metadata.modInfo, this.metadata.resourcePacks);
			if (this.cacheExists()) {
				this.setStatus(Status.LOAD);
				this.loadDashCache();
			} else {
				this.setStatus(Status.SAVE);
			}

			LOG.info("Reloaded DashLoader");
			this.shouldReload = false;
		}
	}

	public void requestReload() {
		this.shouldReload = true;
	}

	public void resetDashLoader() {
		this.setStatus(Status.NONE);
	}

	public void saveDashCache() {
		DashToast.STATUS = DashToast.Status.CACHING;
		LOG.info("Starting DashLoader Caching");
		try {
			ChunkFactory.C_AMOUNT = 0;
			ChunkFactory.C_DE = 0;
			ProgressHandler.INSTANCE.setOverwriteText(null);
			long start = System.currentTimeMillis();

			StepTask main = new StepTask("save", 2);
			ProgressHandler.INSTANCE.task = main;

			// missing model callback
			List<MissingHandler<?>> handlers = new ArrayList<>();
			for (DashEntrypoint entryPoint : FabricLoader.getInstance().getEntrypoints("dashloader", DashEntrypoint.class)) {
				entryPoint.onDashLoaderSave(handlers);
			}
			RegistryFactory factory = RegistryFactory.create(handlers, APIHandler.INSTANCE.getDashObjects());

			// Mappings
			mappingsSerializer.save(getCacheDir(), factory, main);
			main.next();

			// serialization
			main.run(new StepTask("serialize", 2), (task) -> {
				try {
					CacheInfo info = this.registrySerializer.serialize(getCacheDir(), factory, task::setSubTask);
					task.next();
					this.metadataSerializer.save(getCacheDir().resolve(METADATA_FILE_NAME), new StepTask("hi"), info);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				task.next();
			});

			String text = "Created cache in " + TimeUtil.getTimeStringFromStart(start);
			ProgressHandler.INSTANCE.setOverwriteText(text);
			LOG.info(text);
			DashToast.STATUS = DashToast.Status.DONE;

			DashLoader.LOG.info("Collections: " + ChunkFactory.C_AMOUNT + ", Removed " + ChunkFactory.C_DE);
			DashLoader.LOG.info("Quads: " + ChunkFactory.Q_AMOUNT + ", Removed " + ChunkFactory.Q_DE);

		} catch (Throwable thr) {
			this.setStatus(Status.SAVE);
			LOG.error("Failed caching", thr);
			DashToast.STATUS = DashToast.Status.CRASHED;
			this.clearCache();
		}
	}

	private void loadDashCache() {
		var start = System.currentTimeMillis();
		LOG.info("Starting DashLoader Deserialization");
		try {
			StepTask task = new StepTask("Loading DashCache", 3);
			ProgressHandler.INSTANCE.task = task;
			Path cacheDir = getCacheDir();

			// Get metadata
			Path metadataPath = cacheDir.resolve(METADATA_FILE_NAME);
			CacheInfo info = metadataSerializer.load(metadataPath);
			info.countLoaded += 1;
			info.timeLastLoaded = System.currentTimeMillis();
			metadataSerializer.save(metadataPath, new StepTask("none"), info);

			// File reading
			var tempStart = System.currentTimeMillis();
			LOG.info("Reading files");
			StageData[] stageData = registrySerializer.deserialize(cacheDir, info, APIHandler.INSTANCE.getDashObjects());
			RegistryReader reader = new RegistryReader(info, stageData);
			ProfilerHandler.INSTANCE.exportFileReadingTime = System.currentTimeMillis() - tempStart;

			// Exporting assets
			tempStart = System.currentTimeMillis();
			LOG.info("Exporting assets");
			task.run(() -> {
				reader.export(task::setSubTask);
			});
			ProfilerHandler.INSTANCE.exportAssetExportingTime = System.currentTimeMillis() - tempStart;

			// Loading mappings
			tempStart = System.currentTimeMillis();
			LOG.info("Loading Mappings");
			if (!mappingsSerializer.load(cacheDir, reader, APIHandler.INSTANCE.getCacheHandlers())) {
				this.setStatus(Status.SAVE);
				this.clearCache();
				return;
			}
			ProfilerHandler.INSTANCE.exportAssetLoadingTime = System.currentTimeMillis() - tempStart;
			ProfilerHandler.INSTANCE.exportTime = System.currentTimeMillis() - start;
			LOG.info("Loaded DashLoader in {}ms", ProfilerHandler.INSTANCE.exportTime);
		} catch (Exception e) {
			LOG.error("Summoned CrashLoader in {}", TimeUtil.getTimeStringFromStart(start), e);
			this.setStatus(Status.SAVE);
			this.clearCache();
		}
	}

	public void clearCache() {
		try {
			FileUtils.deleteDirectory(this.getCacheDir().toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean cacheExists() {
		return Files.exists(this.getCacheDir());
	}

	public Path getCacheDir() {
		if (this.metadata.resourcePacks == null) {
			throw new RuntimeException("Current SubCache has not been set.");
		}
		return this.getCacheAreaDir().resolve(this.metadata.resourcePacks + "/");
	}

	public Path getCacheAreaDir() {
		if (this.metadata.modInfo == null) {
			throw new RuntimeException("Current Cache has not been set.");
		}
		return this.cacheDir.resolve(this.metadata.modInfo + "/");
	}

	private void setStatus(Status status) {
		LOG.info("\u001B[46m\u001B[30m DashLoader Status change {}\n\u001B[0m", status);
		this.status = status;
		APIHandler.INSTANCE.getCacheHandlers().forEach(handler -> handler.reset(status));
	}

	public boolean isWrite() {
		return this.status == Status.SAVE;
	}

	public boolean isRead() {
		return this.status == Status.LOAD;
	}

	public Status getStatus() {
		return this.status;
	}

	public enum Status {
		NONE,
		LOAD,
		SAVE,
	}

	public static class DashMetadata {
		public String modInfo = "bootstrap";
		public String resourcePacks = "bootstrap";

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
