package dev.quantumfusion.dashloader.def.data.blockstate.property.value;

import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.hyphen.scan.annotations.Data;

@Data
@DashObject(Integer.class)
public record DashIntValue(Integer value) implements DashPropertyValue {
	@Override
	public Integer export(DashRegistryReader exportHandler) {
		return value;
	}
}
