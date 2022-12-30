package dev.quantumfusion.dashloader.minecraft.model.fallback;

import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.minecraft.model.DashModel;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import net.minecraft.client.render.model.BakedModel;

@DashObject(MissingDashModel.class)
public class DashMissingDashModel implements DashModel {
	@Override
	public BakedModel export(RegistryReader reader) {
		return new MissingDashModel();
	}
}
