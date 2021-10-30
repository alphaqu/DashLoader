package dev.quantumfusion.dashloader.def.api;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.def.DashLoader;
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

public class DashLoaderAPI {
	public static final Logger LOGGER = LogManager.getLogger();
	public final List<Class<? extends Dashable<?>>> dashObjects;
	private boolean initialized = false;
	private boolean failed = false;

	public DashLoaderAPI(DashLoader manager) {
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
		LOGGER.info("Added " + dashClass.getSimpleName());
		dashObjects.add(dashClass);
	}


	public void initAPI() {
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

			LOGGER.info("[" + Duration.between(start, Instant.now()).toMillis() + "ms] Initialized api.");
			initialized = true;
		}
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
}