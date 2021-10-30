package dev.quantumfusion.dashloader.def.data.blockstate.property;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import net.minecraft.state.property.Property;

public interface DashProperty extends Dashable<Property<?>> {
	Property<?> export(DashRegistryReader exportHandler);
}
