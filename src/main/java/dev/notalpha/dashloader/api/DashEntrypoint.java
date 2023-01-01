package dev.notalpha.dashloader.api;

import dev.notalpha.dashloader.registry.factory.MissingHandler;

import java.util.List;

public interface DashEntrypoint {
	void onDashLoaderInit(APIHandler apiHandler);

	void onDashLoaderSave(List<MissingHandler<?>> handlers);
}
