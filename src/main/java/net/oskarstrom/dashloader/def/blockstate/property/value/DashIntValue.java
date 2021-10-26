package net.oskarstrom.dashloader.def.blockstate.property.value;

import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.def.api.DashObject;

@DashObject(Integer.class)
public record DashIntValue(Integer value) implements DashPropertyValue {
	@Override
	public Integer toUndash(DashExportHandler exportHandler) {
		return value;
	}
}
