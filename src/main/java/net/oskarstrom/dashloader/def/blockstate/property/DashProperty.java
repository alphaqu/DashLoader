package net.oskarstrom.dashloader.def.blockstate.property;

import net.minecraft.state.property.Property;
import net.oskarstrom.dashloader.api.Dashable;
import net.oskarstrom.dashloader.api.registry.DashRegistry;

public interface DashProperty extends Dashable<Property<?>> {
	Property<?> toUndash(DashRegistry registry);
}
