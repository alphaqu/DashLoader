package net.oskarstrom.dashloader.def.blockstate.property.value;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.def.api.DashObject;

@DashObject(Boolean.class)
public record DashBooleanValue(Boolean value) implements DashPropertyValue {

	@Override
	public Boolean toUndash(DashExportHandler exportHandler) {
		return value;
	}
}
