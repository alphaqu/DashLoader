package dev.quantumfusion.dashloader.def.data.blockstate.property.value;

import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.util.math.Direction;

@Data
@DashObject(Direction.class)
public record DashDirectionValue(Direction direction) implements DashPropertyValue {

	@Override
	public Direction export(DashRegistryReader exportHandler) {
		return this.direction;
	}
}
