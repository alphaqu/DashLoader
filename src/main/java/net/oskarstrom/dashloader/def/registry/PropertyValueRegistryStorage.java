package net.oskarstrom.dashloader.def.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.registry.FactoryConstructor;
import net.oskarstrom.dashloader.core.registry.RegistryStorage;
import net.oskarstrom.dashloader.core.registry.RegistryStorageFactory;
import net.oskarstrom.dashloader.core.registry.FactoryConstructorImpl;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashEnumValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

public class PropertyValueRegistryStorage<F, D extends Dashable<F>> extends RegistryStorageFactory.FactoryRegistryImpl<F, D> {
	protected PropertyValueRegistryStorage(Object2ObjectMap<Class<F>, FactoryConstructor<F, D>> constructor, DashRegistry registry) {
		super(constructor, registry);
	}

	@Override
	public D create(F object, DashRegistry registry) {
		if (object instanceof Enum<?> enumObject) {
			return (D) new DashEnumValue(enumObject);
		}
		return super.create(object, registry);
	}

	public static <F, D extends Dashable<F>> RegistryStorage<F> create(DashRegistry registry, Collection<Map.Entry<Class<? extends F>, Class<? extends D>>> classes) {
		Object2ObjectMap<Class<F>, FactoryConstructor<F, D>> constructors = new Object2ObjectOpenHashMap<>((int) (classes.size() / 0.75f));
		for (var rawDashEntry : classes) {
			//noinspection unchecked
			constructors.put((Class<F>) rawDashEntry.getKey(), getConstructor(rawDashEntry.getKey(), rawDashEntry.getValue()));
		}
		return new PropertyValueRegistryStorage<>(constructors, registry);
	}
	@NotNull
	private static <F, D extends Dashable<F>> FactoryConstructor<F, D> getConstructor(Class<? extends F> rawClass, Class<? extends D> dashClass) {
		try {
			return FactoryConstructorImpl.createConstructor(rawClass, dashClass);
			//TODO error handling
		} catch (IllegalAccessException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

}
