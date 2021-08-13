package net.oskarstrom.dashloader.def.blockstate.property;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.state.property.IntProperty;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.def.api.DashObject;

import java.util.Objects;

@DashObject(IntProperty.class)
public class DashIntProperty implements DashProperty {

	@Serialize(order = 0)
	public final String name;

	@Serialize(order = 1)
	public final int lowest;

	@Serialize(order = 2)
	public final int highest;


	public DashIntProperty(@Deserialize("name") String name,
						   @Deserialize("lowest") int lowest,
						   @Deserialize("highest") int highest) {
		this.name = name;
		this.lowest = lowest;
		this.highest = highest;
	}

	public DashIntProperty(IntProperty property) {
		name = property.getName();
		int lowest = -1;
		int highest = -1;
		for (Integer integer : property.getValues()) {
			if (integer > highest || highest == -1) {
				highest = integer;
			}
			if (integer < lowest || lowest == -1) {
				lowest = integer;
			}
		}
		this.lowest = lowest;
		this.highest = highest;
	}

	@Override
	public IntProperty toUndash(DashRegistry registry) {
		return IntProperty.of(name, lowest, highest);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DashIntProperty that = (DashIntProperty) o;
		return lowest == that.lowest && highest == that.highest && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, lowest, highest);
	}
}
