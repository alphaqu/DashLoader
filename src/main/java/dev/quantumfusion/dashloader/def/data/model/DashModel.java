package dev.quantumfusion.dashloader.def.data.model;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import net.minecraft.client.render.model.BakedModel;

@DashObject(BakedModel.class)
public interface DashModel extends Dashable<BakedModel> {
	BakedModel export(DashRegistryReader reader);
}
