package net.oskarstrom.dashloader.def.model;

import net.minecraft.client.render.model.BakedModel;
import net.oskarstrom.dashloader.api.Dashable;
import net.oskarstrom.dashloader.api.registry.DashRegistry;

public interface DashModel extends Dashable<BakedModel> {
	BakedModel toUndash(DashRegistry registry);

	default void apply(DashRegistry registry) {
	}


	default int getStage() {
		return 0;
	}

}
