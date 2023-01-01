package dev.notalpha.dashloader.api.entrypoint;

import dev.notalpha.dashloader.api.APIHandler;
import dev.notalpha.dashloader.registry.factory.MissingHandler;

import java.util.List;

public interface DashEntrypoint {
	void onDashLoaderInit(APIHandler apiHandler);

	void onDashLoaderSave(List<MissingHandler<?>> handlers);
}
