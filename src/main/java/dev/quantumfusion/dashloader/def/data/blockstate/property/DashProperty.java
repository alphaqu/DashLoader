package dev.quantumfusion.dashloader.def.data.blockstate.property;

import net.minecraft.state.property.Property;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;

public interface DashProperty extends Dashable<Property<?>> {
	Property<?> toUndash(DashExportHandler exportHandler);
}
