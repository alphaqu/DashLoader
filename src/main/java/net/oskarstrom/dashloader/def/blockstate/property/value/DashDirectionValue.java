package net.oskarstrom.dashloader.def.blockstate.property.value;

import net.oskarstrom.dashloader.def.api.DashObject;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.util.math.Direction;
import net.oskarstrom.dashloader.api.registry.DashRegistry;

@DashObject(Direction.class)
public class DashDirectionValue implements DashPropertyValue {
	@Serialize(order = 0)
	public final byte direction;

	public DashDirectionValue(@Deserialize("direction") byte direction) {
		this.direction = direction;
	}

	public DashDirectionValue(Direction direction) {
		this.direction = (byte) direction.getId();
	}


	@Override
	public Direction toUndash(DashRegistry registry) {
		return Direction.byId(direction);
	}
}
