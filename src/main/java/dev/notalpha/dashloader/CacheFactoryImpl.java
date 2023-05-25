package dev.notalpha.dashloader;

import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.cache.Cache;
import dev.notalpha.dashloader.api.cache.CacheFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CacheFactoryImpl implements CacheFactory {
	private static final Logger LOGGER = LogManager.getLogger("CacheFactory");
	private final List<DashObjectClass<?, ?>> dashObjects;
	private final List<DashModule<?>> modules;
	private boolean failed = false;

	public CacheFactoryImpl() {
		this.dashObjects = new ArrayList<>();
		this.modules = new ArrayList<>();
	}

	public void addDashObject(Class<? extends DashObject<?>> dashClass) {
		final Class<?>[] interfaces = dashClass.getInterfaces();
		if (interfaces.length == 0) {
			LOGGER.error("No DashObject interface found. Class: {}", dashClass.getSimpleName());
			this.failed = true;
			return;
		}
		this.dashObjects.add(new DashObjectClass<>(dashClass));
	}

	public void addModule(DashModule<?> module) {
		this.modules.add(module);
	}

	public Cache build(Path cacheDir) {
		if (this.failed) {
			throw new RuntimeException("Failed to initialize the API");
		}

		// Set dashobject ids
		this.dashObjects.sort(Comparator.comparing(o -> o.getDashClass().getName()));
		this.modules.sort(Comparator.comparing(o -> o.getDataClass().getName()));
		List<DashObjectClass<?, ?>> objects = this.dashObjects;
		for (int i = 0; i < objects.size(); i++) {
			DashObjectClass<?, ?> dashObject = objects.get(i);
			dashObject.dashObjectId = i;
		}

		return new CacheImpl(cacheDir.resolve(DashLoader.MOD_HASH + "/"), modules, dashObjects);

	}
}