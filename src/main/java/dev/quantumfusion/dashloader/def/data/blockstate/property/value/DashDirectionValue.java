package dev.quantumfusion.dashloader.def.data.blockstate.property.value;

import net.minecraft.util.math.Direction;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.annotations.DashObject;

@DashObject(Direction.class)
public record DashDirectionValue(Direction direction) implements DashPropertyValue {

	@Override
	public Direction toUndash(DashExportHandler exportHandler) {
		return this.direction;
	}
}
