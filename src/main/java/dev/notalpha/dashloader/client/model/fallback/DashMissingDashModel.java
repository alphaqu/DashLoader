package dev.notalpha.dashloader.client.model.fallback;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.client.model.DashModel;
import dev.notalpha.dashloader.registry.RegistryReader;
import net.minecraft.client.render.model.BakedModel;

@DashObject(MissingDashModel.class)
public class DashMissingDashModel implements DashModel {
	@Override
	public BakedModel export(RegistryReader reader) {
		return new MissingDashModel();
	}
}
