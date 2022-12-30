package dev.quantumfusion.dashloader.api.entrypoint;

import dev.quantumfusion.dashloader.DashMetadata;
import dev.quantumfusion.dashloader.api.APIHandler;
import dev.quantumfusion.dashloader.api.DashCacheHandler;
import dev.quantumfusion.dashloader.api.Dashable;
import dev.quantumfusion.dashloader.registry.factory.MissingHandler;

import java.util.List;
import java.util.function.Consumer;

public interface DashEntrypoint {
	void onDashLoaderInit(APIHandler apiHandler);

	void onDashLoaderSave(List<MissingHandler<?>> handlers);
}
