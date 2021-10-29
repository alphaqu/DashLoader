package dev.quantumfusion.dashloader.def.data.blockstate.property.value;

import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.annotations.DashObject;

@DashObject(Integer.class)
public record DashIntValue(Integer value) implements DashPropertyValue {
	@Override
	public Integer toUndash(DashExportHandler exportHandler) {
		return value;
	}
}
