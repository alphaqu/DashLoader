package dev.notalpha.dashloader.api;

import dev.notalpha.dashloader.api.entrypoint.DashEntrypoint;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class APIHandler {
	private static final Logger LOGGER = LogManager.getLogger("DashLoaderAPI");
	public static final APIHandler INSTANCE = new APIHandler();
	private final List<DashObjectClass<?, ?>> dashObjects;
	private final List<DashCacheHandler<?>> cacheHandlers;
	private boolean failed = false;

	public APIHandler() {
		this.dashObjects = new ArrayList<>();
		this.cacheHandlers = new ArrayList<>();
		Instant start = Instant.now();

		// Go through the entrypoints
		List<DashEntrypoint> entryPoints = FabricLoader.getInstance().getEntrypoints("dashloader", DashEntrypoint.class);
		for (DashEntrypoint entryPoint : entryPoints) {
			entryPoint.onDashLoaderInit(this);
		}

		if (this.failed) {
			throw new RuntimeException("Failed to initialize the API");
		}

		// Set dashobject ids
		this.dashObjects.sort(Comparator.comparing(o -> o.getDashClass().getName()));
		this.cacheHandlers.sort(Comparator.comparing(o -> o.getDataClass().getName()));
		List<DashObjectClass<?, ?>> objects = this.dashObjects;
		for (int i = 0; i < objects.size(); i++) {
			DashObjectClass<?, ?> dashObject = objects.get(i);
			dashObject.dashObjectId = i;
		}

		LOGGER.info("[" + Duration.between(start, Instant.now()).toMillis() + "ms] Initialized api.");
	}

	public void addDashObject(Class<?> dashClass) {
		final Class<?>[] interfaces = dashClass.getInterfaces();
		if (interfaces.length == 0) {
			LOGGER.error("No DashObject interface found. Class: {}", dashClass.getSimpleName());
			this.failed = true;
			return;
		}
		this.dashObjects.add(new DashObjectClass<>(dashClass));
	}

	public void addCacheHandler(DashCacheHandler<?> handler) {
		this.cacheHandlers.add(handler);
	}

	public List<DashObjectClass<?, ?>> getDashObjects() {
		return this.dashObjects;
	}

	public List<DashCacheHandler<?>> getCacheHandlers() {
		return cacheHandlers;
	}
}