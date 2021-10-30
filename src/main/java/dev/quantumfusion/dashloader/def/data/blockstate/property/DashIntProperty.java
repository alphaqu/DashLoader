package dev.quantumfusion.dashloader.def.data.blockstate.property;

import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.state.property.IntProperty;

@Data
@DashObject(IntProperty.class)
public record DashIntProperty(String name, int lowest, int highest) implements DashProperty {

	public static DashIntProperty create(IntProperty property) {
		var name = property.getName();
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
		return new DashIntProperty(name, lowest, highest);
	}

	@Override
	public IntProperty export(DashRegistryReader exportHandler) {
		return IntProperty.of(name, lowest, highest);
	}
}
