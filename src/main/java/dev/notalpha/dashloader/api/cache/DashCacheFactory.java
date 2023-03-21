package dev.notalpha.dashloader.api.cache;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.RegistryWriter;

import java.util.function.BiFunction;

public interface DashCacheFactory {
	void addDashObject(Class<? extends DashObject<?>> dashClass);

	void addModule(DashModule<?> handler);

	<R> void addMissingHandler(Class<R> parentClass, BiFunction<R, RegistryWriter, DashObject<?>> func);
}
