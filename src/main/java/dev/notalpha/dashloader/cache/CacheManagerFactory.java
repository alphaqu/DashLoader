package dev.notalpha.dashloader.cache;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.DashCacheHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CacheManagerFactory {
	private static final Logger LOGGER = LogManager.getLogger("DashLoaderAPI");
	private final List<DashObjectClass<?, ?>> dashObjects;
	private final List<DashCacheHandler<?>> cacheHandlers;
	private boolean failed = false;

	public CacheManagerFactory() {
		this.dashObjects = new ArrayList<>();
		this.cacheHandlers = new ArrayList<>();
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

	public CacheManager build(Path cacheDir) {
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

		return new CacheManager(cacheDir.resolve(DashLoader.MOD_HASH + "/"), cacheHandlers, dashObjects);

	}
}