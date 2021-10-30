package dev.quantumfusion.dashloader.def.data.blockstate.property;

import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.state.property.BooleanProperty;

@Data
@DashObject(BooleanProperty.class)
public record DashBooleanProperty(String name) implements DashProperty {

	public DashBooleanProperty(BooleanProperty property) {
		this(property.getName());
	}

	@Override
	public BooleanProperty export(DashRegistryReader exportHandler) {
		return BooleanProperty.of(name);
	}
}
