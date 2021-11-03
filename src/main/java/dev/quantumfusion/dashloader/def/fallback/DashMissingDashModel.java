package dev.quantumfusion.dashloader.def.fallback;

import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.def.data.model.DashModel;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BakedModel;

@Data
@DashObject(MissingDashModel.class)
public class DashMissingDashModel implements DashModel {
	@Override
	public BakedModel export(DashRegistryReader reader) {
		return new MissingDashModel();
	}
}
