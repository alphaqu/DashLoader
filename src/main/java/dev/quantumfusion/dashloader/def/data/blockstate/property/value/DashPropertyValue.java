package dev.quantumfusion.dashloader.def.data.blockstate.property.value;

import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;

public interface DashPropertyValue extends Dashable<Comparable<?>> {
	Comparable<?> toUndash(DashExportHandler exportHandler);
}

