package dev.quantumfusion.dashloader.def.data.blockstate.property.value;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;

public interface DashPropertyValue extends Dashable<Comparable<?>> {
	Comparable<?> export(DashRegistryReader exportHandler);
}

