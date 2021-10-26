package net.oskarstrom.dashloader.def.model;

import net.minecraft.client.render.model.BakedModel;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;

public interface DashModel extends Dashable<BakedModel> {
	BakedModel toUndash(DashExportHandler exportHandler);

	default void apply(DashRegistry registry) {
	}


	default int getStage() {
		return 0;
	}

}
