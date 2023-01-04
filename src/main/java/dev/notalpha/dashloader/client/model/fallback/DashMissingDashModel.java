package dev.notalpha.dashloader.client.model.fallback;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.registry.RegistryReader;

public class DashMissingDashModel implements DashObject<MissingDashModel> {
	public static final DashMissingDashModel IDENTITY = new DashMissingDashModel();
	public DashMissingDashModel() {
	}

	@Override
	public MissingDashModel export(RegistryReader reader) {
		return new MissingDashModel();
	}
}
