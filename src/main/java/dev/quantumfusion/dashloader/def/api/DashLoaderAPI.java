package dev.quantumfusion.dashloader.def.api;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.def.DashConstants;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class DashLoaderAPI {
	public static final Logger LOGGER = LogManager.getLogger("DashLoaderAPI");
	public final List<Class<? extends Dashable<?>>> dashObjects;
	private boolean initialized = false;
	private boolean failed = false;

	public DashLoaderAPI() {
		this.dashObjects = Collections.synchronizedList(new ArrayList<>());
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
		dashObjects.add(dashClass);
	}


	public void initAPI() {
		if (!initialized) {
			Instant start = Instant.now();
			clearAPI();
			FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
				final ModMetadata metadata = modContainer.getMetadata();
				if (metadata.getCustomValues().size() != 0) {
					CustomValue value = metadata.getCustomValue("dashloader:customobject");
					if (value != null) LOGGER.error("Found DashLoader 2.0 mod: " + modContainer.getMetadata().getId());

					applyForClassesInValue(metadata, DashConstants.DASH_OBJECT_TAG, this::registerDashObject);
				}
			});

			if (failed)
				throw new RuntimeException("Failed to initialize the API");

			LOGGER.info("[" + Duration.between(start, Instant.now()).toMillis() + "ms] Initialized api.");
			initialized = true;
		}
	}


	private <F, D extends Dashable<F>> void applyForClassesInValue(ModMetadata modMetadata, String valueName, Consumer<Class<D>> func) {
		var value = modMetadata.getCustomValue(valueName);
		if (value != null) {
			for (CustomValue customValue : value.getAsArray()) {
				final String dashObject = customValue.getAsString();
				try {
					final Class<D> closs = (Class<D>) Class.forName(dashObject, true, Thread.currentThread().getContextClassLoader());
					func.accept(closs);
				} catch (ClassNotFoundException e) {
					LOGGER.error("Class not found, Mod: \"{}\", Value: \"{}\"", modMetadata.getId(), customValue.getAsString());
					failed = true;
				} catch (Throwable throwable) {
					LOGGER.error("Failed to load class. Mod: \"{}\", Value: \"{}\"", modMetadata.getId(), customValue.getAsString());
				}
			}
		}
	}
}