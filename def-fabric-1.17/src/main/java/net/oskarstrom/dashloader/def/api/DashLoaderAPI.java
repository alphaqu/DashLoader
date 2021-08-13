package net.oskarstrom.dashloader.def.api;

import io.activej.serializer.SerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.oskarstrom.dashloader.api.Dashable;
import net.oskarstrom.dashloader.api.registry.FactoryConstructor;
import net.oskarstrom.dashloader.core.registry.FactoryConstructorImpl;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.blockstate.property.DashBooleanProperty;
import net.oskarstrom.dashloader.def.blockstate.property.DashDirectionProperty;
import net.oskarstrom.dashloader.def.blockstate.property.DashEnumProperty;
import net.oskarstrom.dashloader.def.blockstate.property.DashIntProperty;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashBooleanValue;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashDirectionValue;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashEnumValue;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashIntValue;
import net.oskarstrom.dashloader.def.font.DashBitmapFont;
import net.oskarstrom.dashloader.def.font.DashBlankFont;
import net.oskarstrom.dashloader.def.font.DashTrueTypeFont;
import net.oskarstrom.dashloader.def.font.DashUnicodeFont;
import net.oskarstrom.dashloader.def.model.DashBasicBakedModel;
import net.oskarstrom.dashloader.def.model.DashBuiltinBakedModel;
import net.oskarstrom.dashloader.def.model.DashMultipartBakedModel;
import net.oskarstrom.dashloader.def.model.DashWeightedBakedModel;
import net.oskarstrom.dashloader.def.model.predicates.DashAndPredicate;
import net.oskarstrom.dashloader.def.model.predicates.DashOrPredicate;
import net.oskarstrom.dashloader.def.model.predicates.DashSimplePredicate;
import net.oskarstrom.dashloader.def.model.predicates.DashStaticPredicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("removal")
public class DashLoaderAPI {
	public static final Logger LOGGER = LogManager.getLogger();
	public final Map<DashDataType, Map<Class<?>, FactoryConstructor<?,?>>> mappings;
	public final List<DashDataClass> dataClasses;

	private boolean initialized = false;
	private boolean failed = false;

	public DashLoaderAPI() {
		mappings = Collections.synchronizedMap(new HashMap<>());
		dataClasses = Collections.synchronizedList(new ArrayList<>());
	}


	private void clearAPI() {
		mappings.clear();
		dataClasses.clear();
	}

	private void addType(DashDataType type, Class<?> dashClass) {
		DashLoader.getInstance().getCoreManager().getSerializerManager().addSubclass(type.internalName, dashClass);
	}

	private void addFactoryToType(DashDataType type, Class<?> dashClass, Class<?> targetClass, FactoryConstructor<?,?> constructor) {
		addType(type, dashClass);
		mappings.computeIfAbsent(type, type1 -> Collections.synchronizedMap(new HashMap<>())).put(targetClass, constructor);
		LOGGER.info("Added custom DashObject: {} {}", type, dashClass.getSimpleName());
	}


	private void addDataObjectToType(DashDataType type, Class<?> dataClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		addType(type, dataClass);
		dataClasses.add((DashDataClass) dataClass.getDeclaredConstructor().newInstance());
		LOGGER.info("Added custom DashDataObject: {}", dataClass.getSimpleName());
	}

	private DashDataType getTypeFromFactoryInterface(Class<?> closs) {
		for (DashDataType value : DashDataType.values()) {
			if (value.factoryInterface == closs) {
				return value;
			}
		}
		LOGGER.error("Cannot find Factory Type from {} class parameter.", closs.getSimpleName());
		failed = true;
		return null;
	}

	public <F,D extends Dashable<F>> void registerDashObject(Class<D> dashClass) {
		final Class<?>[] interfaces = dashClass.getInterfaces();
		if (interfaces.length == 0) {
			LOGGER.error("No Interfaces found. Class: {}", dashClass.getSimpleName());
			failed = true;
			return;
		}
		final DashObject annotation = dashClass.getDeclaredAnnotation(DashObject.class);
		if (annotation == null) {
			LOGGER.error("Custom DashObject implementation does not have DashObject Annotation. Class: {}", dashClass.getSimpleName());
			failed = true;
			return;
		}
		DashDataType type = getTypeFromFactoryInterface(interfaces[0]);

		if (type == null) {
			LOGGER.error("Factory type could not be identified. Class: {}", dashClass.getSimpleName());
			failed = true;
			return;
		}
		if (type.requiresTargetObject) {
			if (annotation.value() == NullPointerException.class) {
				LOGGER.error("The type {} requires a target object in the @DashObject annotation", type.name);
				failed = true;
				return;
			}
		}
		if (type != DashDataType.DATA) {
			final Class<F> rawClass = (Class<F>) annotation.value();
			try {
				addFactoryToType(type, dashClass, rawClass, FactoryConstructorImpl.createConstructor(rawClass,dashClass));
			} catch (NoSuchMethodException e) {
				LOGGER.error("Constructor not matching/found. Expected: {}", e.getMessage());
				failed = true;
			} catch (IllegalAccessException e) {
				LOGGER.error("Constructor not accessible in {}", dashClass.getSimpleName());
				failed = true;
			}
		} else {
			try {
				addDataObjectToType(type, dashClass);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
				failed = true;
			}
		}
	}


	private void initNativeAPI() {
		registerDashObject(DashBasicBakedModel.class);
		registerDashObject(DashBuiltinBakedModel.class);
		registerDashObject(DashMultipartBakedModel.class);
		registerDashObject(DashWeightedBakedModel.class);

		registerDashObject(DashAndPredicate.class);
		registerDashObject(DashOrPredicate.class);
		registerDashObject(DashSimplePredicate.class);
		addType(DashDataType.PREDICATE, DashStaticPredicate.class); // still cursed

		registerDashObject(DashBooleanProperty.class);
		registerDashObject(DashDirectionProperty.class);
		registerDashObject(DashEnumProperty.class);
		registerDashObject(DashIntProperty.class);

		registerDashObject(DashBooleanValue.class);
		registerDashObject(DashDirectionValue.class);
		registerDashObject(DashEnumValue.class);
		registerDashObject(DashIntValue.class);

		registerDashObject(DashBitmapFont.class);
		registerDashObject(DashBlankFont.class);
		registerDashObject(DashTrueTypeFont.class);
		registerDashObject(DashUnicodeFont.class);
	}


	public void initAPI() {
		if (!initialized) {
			Instant start = Instant.now();
			clearAPI();
			initNativeAPI();
			FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
				final ModMetadata metadata = modContainer.getMetadata();
				if (metadata.getCustomValues().size() != 0) {
					applyForClassesInValue(metadata, "dashloader:customobject", this::registerDashObject);
				}
			});
			if (failed) {
				throw new RuntimeException("Failed to initialize the API");
			}
			LOGGER.info("[" + Duration.between(start, Instant.now()).toMillis() + "ms] Initialized api.");
			initialized = true;
		}
	}

	private <F,D extends Dashable<F>> void applyForClassesInValue(ModMetadata modMetadata, String valueName, Consumer<Class<D>> func) {
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