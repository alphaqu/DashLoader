package net.oskarstrom.dashloader.def.data.serialize;

import net.oskarstrom.dashloader.api.registry.DashRegistry;

public interface RegistryDataObject {
	void dumpData(DashRegistry dashRegistry);
	int getSize();
}
