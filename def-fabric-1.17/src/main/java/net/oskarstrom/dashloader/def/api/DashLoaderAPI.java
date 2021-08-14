package net.oskarstrom.dashloader.def.api;

import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.oskarstrom.dashloader.api.Dashable;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.registry.RegistryStorage;
import net.oskarstrom.dashloader.core.DashLoaderManager;
import net.oskarstrom.dashloader.def.blockstate.property.DashBooleanProperty;
import net.oskarstrom.dashloader.def.blockstate.property.DashDirectionProperty;
import net.oskarstrom.dashloader.def.blockstate.property.DashEnumProperty;
import net.oskarstrom.dashloader.def.blockstate.property.DashIntProperty;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashBooleanValue;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashDirectionValue;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashEnumValue;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashIntValue;
import net.oskarstrom.dashloader.def.data.DashSerializers;
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

public class DashLoaderAPI {
	public static final Logger LOGGER = LogManager.getLogger();
	public final Map<DashDataType, MappingMap<?, ?>> mappings;
	public final Object2ByteMap<DashDataType> storageMappings;
	public final List<DashDataClass> dataClasses;
	private final DashLoaderManager manager;
	private boolean initialized = false;
	private boolean failed = false;

	public DashLoaderAPI(DashLoaderManager manager) {
		this.manager = manager;
		storageMappings = Object2ByteMaps.synchronize(new Object2ByteOpenHashMap<>());
		mappings = Collections.synchronizedMap(new HashMap<>());
		dataClasses = Collections.synchronizedList(new ArrayList<>());
	}


	private void clearAPI() {
		mappings.clear();
		storageMappings.clear();
		dataClasses.clear();
	}

	private void addType(DashDataType type, Class<?> dashClass) {
		manager.getSerializerManager().addSubclass(type.internalName, dashClass);
	}

	private <F, D extends Dashable<F>> void addFactoryToType(DashDataType type, Class<F> targetClass, Class<D> dashClass) {
		addType(type, dashClass);
		//noinspection unchecked
		final MappingMap<F, D> mappingMap = (MappingMap<F, D>) mappings.computeIfAbsent(type, type1 -> new MappingMap<>(new Object2ObjectOpenHashMap<>()));
		mappingMap.put(targetClass, dashClass);
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

	public <F, D extends Dashable<F>> void registerDashObject(Class<D> dashClass) {
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
			addFactoryToType(type, rawClass, dashClass);
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

			if (failed)
				throw new RuntimeException("Failed to initialize the API");
			DashSerializers.initSerializers();
			mappings.forEach(this::addRegistryStorage);
			LOGGER.info("[" + Duration.between(start, Instant.now()).toMillis() + "ms] Initialized api.");
			initialized = true;
		}
	}

	private <F, D extends Dashable<F>> void addRegistryStorage(DashDataType data, MappingMap<F, D> map) {
		final DashRegistry registry = manager.getRegistry();
		final RegistryStorage<F> multiRegistry = manager.getStorageManager().createMultiRegistry(map, registry);

		//add registry storage to the registry
		final byte storagePointer = registry.addStorage(multiRegistry);

		//create all pointers to that registry
		map.keySet().forEach(fClass -> registry.addMapping(fClass, storagePointer));

		//save that registry for future use
		storageMappings.put(data, storagePointer);
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

	private static class MappingMap<T, D extends Dashable<T>> extends AbstractObject2ObjectMap<Class<T>, Class<D>> {

		private final Object2ObjectMap<Class<T>, Class<D>> delegate;

		private MappingMap(Object2ObjectMap<Class<T>, Class<D>> delegate) {
			this.delegate = delegate;
		}

		@Override
		public int size() {
			return delegate.size();
		}

		@Override
		public ObjectSet<Entry<Class<T>, Class<D>>> object2ObjectEntrySet() {
			return delegate.object2ObjectEntrySet();
		}


		@Override
		public Class<D> get(Object key) {
			return delegate.get(key);
		}

		@Override
		public Class<D> put(Class<T> key, Class<D> value) {
			return super.put(key, value);

		}
	}


}