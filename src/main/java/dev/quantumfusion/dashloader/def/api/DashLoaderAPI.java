package dev.quantumfusion.dashloader.def.api;

import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.DashSerializers;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.registry.DashRegistryBuilder;
import net.oskarstrom.dashloader.core.util.ClassLoaderHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DashLoaderAPI {
	public static final Logger LOGGER = LogManager.getLogger();
	public final DashObjectMap<?, ?> dashObjects;
	private boolean initialized = false;
	private boolean failed = false;

	public DashLoaderAPI(DashLoader manager) {
		this.dashObjects = new DashObjectMap<>(new Object2ObjectOpenHashMap<>());
	}


	private void clearAPI() {
		dashObjects.clear();
	}

	public <F, D extends Dashable<F>> void registerDashObject(Class<D> dashClass) {
		final Class<?>[] interfaces = dashClass.getInterfaces();
		if (interfaces.length == 0) {
			LOGGER.error("No Interfaces found. Class: {}", dashClass.getSimpleName());
			failed = true;
			return;
		}
		dashObjects.add();


	}


	public void initAPI(DashRegistry registry) {
		if (!initialized) {
			Instant start = Instant.now();
			clearAPI();
			FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
				final ModMetadata metadata = modContainer.getMetadata();
				if (metadata.getCustomValues().size() != 0) {
					applyForClassesInValue(metadata, "dashloader:customobject", this::registerDashObject);
				}
			});

			if (failed)
				throw new RuntimeException("Failed to initialize the API");
			DashSerializers.initSerializers();

			LOGGER.info("[" + Duration.between(start, Instant.now()).toMillis() + "ms] Initialized api.");
			initialized = true;
		}
	}

	public DashRegistry createRegistry() {
		ClassLoaderHelper.init();
		final DashRegistryBuilder factory = DashRegistryBuilder.create();
		// Add dash objects
		dashObjects.forEach((dashDataType, mappingMap) -> factory.withDashObjects(mappingMap.values().toArray(Class[]::new)));
		final DashRegistry registry = factory.build();

	}


	private <F, D extends Dashable<F>> void applyForClassesInValue(ModMetadata modMetadata, String valueName, Consumer<Class<D>> func) {
		CustomValue value = modMetadata.getCustomValue(valueName);
		if (value != null) {
			for (CustomValue customValue : value.getAsArray()) {
				final String dashObject = customValue.getAsString();
				try {
					final Class<D> closs = (Class<D>) Class.forName(dashObject);
					func.accept(closs);
				} catch (ClassNotFoundException e) {
					LOGGER.error("Class not found, Mod: \"{}\", Value: \"{}\"", modMetadata.getId(), customValue.getAsString());
					failed = true;
				}
			}
		}
	}

	public static class DashObjectMap<T, D extends Dashable<T>> extends AbstractObject2ObjectMap<Class<? extends T>, List<Class<? extends D>>> {
		private final Object2ObjectMap<Class<? extends T>, List<Class<? extends D>>> delegate;

		private DashObjectMap(Object2ObjectMap<Class<? extends T>, List<Class<? extends D>>> delegate) {
			this.delegate = delegate;
		}

		@Override
		public int size() {
			return delegate.size();
		}

		@Override
		public ObjectSet<Entry<Class<? extends T>, List<Class<? extends D>>>> object2ObjectEntrySet() {
			return delegate.object2ObjectEntrySet();
		}


		@Override
		public List<Class<? extends D>> get(Object key) {
			return delegate.get(key);
		}

		@Override
		public List<Class<? extends D>> put(Class<? extends T> key, List<Class<? extends D>> value) {
			return delegate.put(key, value);
		}

		public void add(Class<? extends T> key, Class<? extends D> value) {
			delegate.computeIfAbsent(key, (c) -> new ArrayList<>()).add(value);
		}
	}


}