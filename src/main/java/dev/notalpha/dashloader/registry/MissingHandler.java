package dev.notalpha.dashloader.registry;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.RegistryWriter;

import java.util.function.BiFunction;

public final class MissingHandler<R> {
	public final Class<R> parentClass;
	public final BiFunction<R, RegistryWriter, DashObject<?>> func;

	public MissingHandler(Class<R> parentClass, BiFunction<R, RegistryWriter, DashObject<?>> func) {
		this.parentClass = parentClass;
		this.func = func;
	}
}
