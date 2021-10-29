package dev.quantumfusion.dashloader.def.data.model;

import net.minecraft.client.render.model.BakedModel;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;

public interface DashModel extends Dashable<BakedModel> {
	BakedModel toUndash(DashExportHandler exportHandler);

	default void apply(DashExportHandler registry) {
	}
}
