package net.oskarstrom.dashloader.def.blockstate.property.value;

import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;

public interface DashPropertyValue extends Dashable<Comparable<?>> {

	Comparable<?> toUndash(DashExportHandler exportHandler);
}

