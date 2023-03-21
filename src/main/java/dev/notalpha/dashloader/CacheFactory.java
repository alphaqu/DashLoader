package dev.notalpha.dashloader;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.RegistryWriter;
import dev.notalpha.dashloader.api.cache.DashCacheFactory;
import dev.notalpha.dashloader.api.cache.DashModule;
import dev.notalpha.dashloader.registry.MissingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

public class CacheFactory implements DashCacheFactory {
	private static final Logger LOGGER = LogManager.getLogger("CacherFactory");
	private final List<DashObjectClass<?, ?>> dashObjects;
	private final List<DashModule<?>> modules;
	private final List<MissingHandler<?>> missingHandlers;
	private boolean failed = false;

	public CacheFactory() {
		this.dashObjects = new ArrayList<>();
		this.modules = new ArrayList<>();
		this.missingHandlers = new ArrayList<>();
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

	public void addModule(DashModule<?> handler) {
		this.modules.add(handler);
	}

	@Override
	public <R> void addMissingHandler(Class<R> parentClass, BiFunction<R, RegistryWriter, DashObject<?>> func) {
		this.missingHandlers.add(new MissingHandler<>(parentClass, func));
	}

	public Cache build(Path cacheDir) {
		if (this.failed) {
			throw new RuntimeException("Failed to initialize the API");
		}

		// Set dashobject ids
		this.dashObjects.sort(Comparator.comparing(o -> o.getDashClass().getName()));
		this.modules.sort(Comparator.comparing(o -> o.getDataClass().getName()));
		this.missingHandlers.sort(Comparator.comparing(o -> o.parentClass.getName()));
		List<DashObjectClass<?, ?>> objects = this.dashObjects;
		for (int i = 0; i < objects.size(); i++) {
			DashObjectClass<?, ?> dashObject = objects.get(i);
			dashObject.dashObjectId = i;
		}

		return new Cache(cacheDir.resolve(DashLoader.MOD_HASH + "/"), modules, dashObjects, missingHandlers);

	}
}