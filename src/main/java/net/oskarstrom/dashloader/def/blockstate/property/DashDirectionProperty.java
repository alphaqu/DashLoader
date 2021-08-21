package net.oskarstrom.dashloader.def.blockstate.property;

import net.oskarstrom.dashloader.def.api.DashObject;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.Direction;
import net.oskarstrom.dashloader.api.registry.DashRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@DashObject(DirectionProperty.class)
public class DashDirectionProperty implements DashProperty {
	@Serialize(order = 0)
	public final String name;

	@Serialize(order = 1)
	@SerializeNullable
	public final Direction[] directions;


	public DashDirectionProperty(@Deserialize("name") String name,
								 @Deserialize("directions") Direction[] directions) {
		this.name = name;
		this.directions = directions;
	}

	public DashDirectionProperty(DirectionProperty property) {
		name = property.getName();
		final Collection<Direction> values = property.getValues();
		final int size = values.size();
		if (size == 6) {
			this.directions = null;
		} else {
			List<Direction> directionsOut = new ArrayList<>(values);
			this.directions = new Direction[directionsOut.size()];
			for (int i = 0; i < directionsOut.size(); i++) {
				this.directions[i] = directionsOut.get(i);
			}
		}
	}

	@Override
	public DirectionProperty toUndash(DashRegistry registry) {
		return DirectionProperty.of(name, directions == null ? Direction.values() : directions);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DashDirectionProperty that = (DashDirectionProperty) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
