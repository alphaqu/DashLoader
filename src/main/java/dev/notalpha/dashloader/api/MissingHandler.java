package dev.notalpha.dashloader.api;

import dev.notalpha.dashloader.registry.RegistryWriter;

import java.util.function.BiFunction;

public class MissingHandler<R> {
	public final Class<R> parentClass;
	public final BiFunction<R, RegistryWriter, Exportable<?>> func;

	public MissingHandler(Class<R> parentClass, BiFunction<R, RegistryWriter, Exportable<?>> func) {
		this.parentClass = parentClass;
		this.func = func;
	}
}
