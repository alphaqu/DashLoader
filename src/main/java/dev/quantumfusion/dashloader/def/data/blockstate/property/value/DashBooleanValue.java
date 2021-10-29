package dev.quantumfusion.dashloader.def.data.blockstate.property.value;

import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.annotations.DashObject;

@DashObject(Boolean.class)
public record DashBooleanValue(Boolean value) implements DashPropertyValue {

	@Override
	public Boolean toUndash(DashExportHandler exportHandler) {
		return value;
	}
}
