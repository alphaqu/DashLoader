package dev.quantumfusion.dashloader.api;

import dev.quantumfusion.dashloader.DashConstants;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.api.hook.LoadCacheHook;
import dev.quantumfusion.dashloader.api.hook.SaveCacheHook;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

public class APIHandler {
	public static final Logger LOGGER = LogManager.getLogger("DashLoaderAPI");
	public static final Class<?>[] HOOK_CLASSES = {SaveCacheHook.class, LoadCacheHook.class};
	private final Map<Class<?>, List<Object>> hookSubscribers;
	private final List<DashObjectClass<?, ?>> dashObjects;
	private boolean initialized = false;
	private boolean failed = false;

	public APIHandler() {
		this.dashObjects = new ArrayList<>();
		this.hookSubscribers = new HashMap<>();
	}

	public <F, D extends Dashable<F>> void registerDashObject(Class<D> dashClass) {
		final Class<?>[] interfaces = dashClass.getInterfaces();
		if (interfaces.length == 0) {
			LOGGER.error("No Interfaces found. Class: {}", dashClass.getSimpleName());
			this.failed = true;
			return;
		}
		this.dashObjects.add(new DashObjectClass<>(dashClass));
	}

	public <H> void registerHook(Class<H> hookClass) {
		H hook;
		try {
			hook = hookClass.getDeclaredConstructor().newInstance();
		} catch (NoSuchMethodException e) {
			LOGGER.error("Could not find constructor. {}", hookClass.getSimpleName());
			this.failed = true;
			return;
		} catch (InvocationTargetException | InstantiationException e) {
			LOGGER.error("Constructor error.", e);
			this.failed = true;
			return;
		} catch (IllegalAccessException e) {
			LOGGER.error("Constructor not accessible. {}", hookClass.getSimpleName());
			this.failed = true;
			return;
		}

		for (Class<?> aClass : HOOK_CLASSES) {
			if (aClass.isInstance(hook)) {
				this.hookSubscribers.computeIfAbsent(aClass, (c) -> new ArrayList<>()).add(hook);
				LOGGER.info("Registered {} as {}", hookClass.getSimpleName(), aClass.getSimpleName());
			}
		}
	}

	public <H> void callHook(Class<H> hookClass, Consumer<H> consumer) {
		List<Object> objects = this.hookSubscribers.get(hookClass);
		if (objects != null) {
			//noinspection unchecked
			objects.forEach(o -> consumer.accept((H) o));
		}
	}

	public void initAPI() {
		if (!this.initialized) {
			Instant start = Instant.now();
			this.dashObjects.clear();
			FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
				final ModMetadata metadata = modContainer.getMetadata();
				if (metadata.getCustomValues().size() != 0) {
					this.applyForClassesInValue(metadata, DashConstants.DASH_OBJECT_TAG, this::registerDashObject);
					this.applyForClassesInValue(metadata, DashConstants.DASH_HOOK_TAG, this::registerHook);
				}
			});

			if (this.failed) {
				throw new RuntimeException("Failed to initialize the API");
			}

			this.dashObjects.sort(Comparator.comparing(o -> o.getDashClass().getSimpleName()));

			LOGGER.info("[" + Duration.between(start, Instant.now()).toMillis() + "ms] Initialized api.");
			List<DashObjectClass<?, ?>> objects = this.dashObjects;
			for (int i = 0; i < objects.size(); i++) {
				DashObjectClass<?, ?> dashObject = objects.get(i);
				dashObject.dashObjectId = i;
			}
			this.initialized = true;
		}
	}


	private <F, D extends Dashable<F>> void applyForClassesInValue(ModMetadata modMetadata, String valueName, Consumer<Class<D>> func) {
		var value = modMetadata.getCustomValue(valueName);
		if (value != null) {
			for (CustomValue customValue : value.getAsArray()) {
				final String dashObject = customValue.getAsString();
				try {
					//noinspection unchecked
					final Class<D> closs = (Class<D>) Class.forName(dashObject, true, Thread.currentThread().getContextClassLoader());
					func.accept(closs);
				} catch (ClassNotFoundException e) {
					LOGGER.error("Class not found, Mod: \"{}\", Value: \"{}\"", modMetadata.getId(), customValue.getAsString());
					this.failed = true;
				} catch (Throwable throwable) {
					LOGGER.error("Failed to load class. Mod: \"{}\", Value: \"{}\"", modMetadata.getId(), customValue.getAsString());
				}
			}
		}
	}

	public List<DashObjectClass<?, ?>> getDashObjects() {
		return this.dashObjects;
	}
}