package dev.notalpha.dashloader.cache.registry.factory;

import dev.notalpha.dashloader.api.Dashable;
import dev.notalpha.dashloader.cache.registry.RegistryWriter;

import java.util.function.BiFunction;

public class MissingHandler<R> {
	public final Class<R> parentClass;
	public final BiFunction<R, RegistryWriter, Dashable<?>> func;

	public MissingHandler(Class<R> parentClass, BiFunction<R, RegistryWriter, Dashable<?>> func) {
		this.parentClass = parentClass;
		this.func = func;
	}
}
