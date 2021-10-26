package net.oskarstrom.dashloader.def.blockstate.property;

import net.minecraft.state.property.BooleanProperty;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.def.api.DashObject;

import java.util.Objects;

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
