package dev.quantumfusion.dashloader.def.data.blockstate.property;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import net.minecraft.client.font.Font;
import net.minecraft.state.property.Property;

@DashObject(Property.class)
public interface DashProperty extends Dashable<Property<?>> {
	Property<?> export(DashRegistryReader exportHandler);
}
