package dev.quantumfusion.dashloader.def.data.blockstate.property;

import net.minecraft.state.property.BooleanProperty;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.annotations.DashObject;

@DashObject(BooleanProperty.class)
public record DashBooleanProperty(String name) implements DashProperty {

	public DashBooleanProperty(BooleanProperty property) {
		this(property.getName());
	}

	@Override
	public BooleanProperty toUndash(DashExportHandler exportHandler) {
		return BooleanProperty.of(name);
	}
}
