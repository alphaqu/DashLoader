package net.oskarstrom.dashloader.def.blockstate.property.value;

import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.def.api.DashObject;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.oskarstrom.dashloader.core.registry.DashRegistry;

@DashObject(Integer.class)
public class DashIntValue implements DashPropertyValue {
	@Serialize(order = 0)
	public final Integer value;

	public DashIntValue(@Deserialize("value") Integer value) {
		this.value = value;
	}


	@Override
	public Integer toUndash(DashExportHandler exportHandler) {
		return value;
	}
}
