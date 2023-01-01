package dev.notalpha.dashloader.minecraft.model.fallback;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.cache.registry.RegistryReader;
import dev.notalpha.dashloader.minecraft.model.DashModel;
import net.minecraft.client.render.model.BakedModel;

@DashObject(MissingDashModel.class)
public class DashMissingDashModel implements DashModel {
	@Override
	public BakedModel export(RegistryReader reader) {
		return new MissingDashModel();
	}
}
