package net.oskarstrom.dashloader.def.blockstate.property;

import net.minecraft.state.property.Property;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;

public interface DashProperty extends Dashable<Property<?>> {
	Property<?> toUndash(DashExportHandler exportHandler);
}
