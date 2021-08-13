package net.oskarstrom.dashloader.def.blockstate.property.value;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.def.api.DashObject;

@DashObject(Integer.class)
public class DashIntValue implements DashPropertyValue {
	@Serialize(order = 0)
	public final Integer value;

	public DashIntValue(@Deserialize("value") Integer value) {
		this.value = value;
	}


	@Override
	public Integer toUndash(DashRegistry registry) {
		return value;
	}
}
