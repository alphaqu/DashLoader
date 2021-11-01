package dev.quantumfusion.dashloader.def.data.blockstate.property.value;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import net.minecraft.state.property.Property;

@DashObject(Comparable.class)
public interface DashPropertyValue extends Dashable<Comparable<?>> {
	Comparable<?> export(DashRegistryReader exportHandler);
}

