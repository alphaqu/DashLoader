package dev.quantumfusion.dashloader.def.data.dataobject;

import net.oskarstrom.dashloader.core.registry.DashExportHandler;

public interface RegistryDataObject {
	void dumpData(DashExportHandler dashRegistry);
	int getSize();
}
