package dev.quantumfusion.dashloader.fallback.model;

import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.data.model.DashModel;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import net.minecraft.client.render.model.BakedModel;

@DashObject(MissingDashModel.class)
public class DashMissingDashModel implements DashModel {
	@Override
	public BakedModel export(RegistryReader reader) {
		return new MissingDashModel();
	}
}
