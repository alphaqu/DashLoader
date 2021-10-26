package net.oskarstrom.dashloader.def.blockstate.property.value;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.util.math.Direction;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.def.api.DashObject;

@DashObject(Direction.class)
public record DashDirectionValue(Direction direction) implements DashPropertyValue {

	@Override
	public Direction toUndash(DashExportHandler exportHandler) {
		return this.direction;
	}
}
