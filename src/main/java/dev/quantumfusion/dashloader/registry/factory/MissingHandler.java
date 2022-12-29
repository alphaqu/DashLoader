package dev.quantumfusion.dashloader.registry.factory;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.RegistryFactory;
import dev.quantumfusion.dashloader.registry.RegistryWriter;

import java.util.function.BiFunction;

public class MissingHandler<R> {
	public final Class<R> parentClass;
	public final BiFunction<R, RegistryWriter, Dashable<?>> func;

	public MissingHandler(Class<R> parentClass, BiFunction<R, RegistryWriter, Dashable<?>> func) {
		this.parentClass = parentClass;
		this.func = func;
	}
}
