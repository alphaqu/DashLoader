package dev.quantumfusion.dashloader.def.data.blockstate.property.value;

import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.hyphen.scan.annotations.Data;

@Data
@DashObject(Boolean.class)
public record DashBooleanValue(Boolean value) implements DashPropertyValue {

	@Override
	public Boolean export(DashRegistryReader exportHandler) {
		return value;
	}
}
