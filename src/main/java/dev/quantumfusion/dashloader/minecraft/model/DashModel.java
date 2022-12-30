package dev.quantumfusion.dashloader.minecraft.model;

import dev.quantumfusion.dashloader.api.Dashable;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import net.minecraft.client.render.model.BakedModel;

@DashObject(BakedModel.class)
public interface DashModel extends Dashable<BakedModel> {
	BakedModel export(RegistryReader reader);
}
