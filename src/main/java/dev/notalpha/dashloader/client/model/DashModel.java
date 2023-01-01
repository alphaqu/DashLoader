package dev.notalpha.dashloader.client.model;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.Exportable;
import dev.notalpha.dashloader.registry.RegistryReader;
import net.minecraft.client.render.model.BakedModel;

@DashObject(BakedModel.class)
public interface DashModel extends Exportable<BakedModel> {
	BakedModel export(RegistryReader reader);
}
