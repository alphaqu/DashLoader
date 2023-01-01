package dev.notalpha.dashloader.minecraft.model;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.Dashable;
import dev.notalpha.dashloader.registry.RegistryReader;
import net.minecraft.client.render.model.BakedModel;

@DashObject(BakedModel.class)
public interface DashModel extends Dashable<BakedModel> {
	BakedModel export(RegistryReader reader);
}
