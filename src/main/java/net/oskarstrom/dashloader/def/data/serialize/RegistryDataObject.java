package net.oskarstrom.dashloader.def.data.serialize;

import net.oskarstrom.dashloader.core.registry.DashExportHandler;

public interface RegistryDataObject {
	void dumpData(DashExportHandler dashRegistry);
	int getSize();
}
