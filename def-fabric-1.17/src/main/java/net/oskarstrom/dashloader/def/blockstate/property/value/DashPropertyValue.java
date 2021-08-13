package net.oskarstrom.dashloader.def.blockstate.property.value;

import net.oskarstrom.dashloader.api.Dashable;
import net.oskarstrom.dashloader.api.registry.DashRegistry;

public interface DashPropertyValue extends Dashable<Comparable<?>> {

	Comparable<?> toUndash(DashRegistry registry);
}

