package dev.quantumfusion.dashloader.def.data.model;

import dev.quantumfusion.dashloader.def.data.model.components.DashWeightedModelEntry;
import dev.quantumfusion.dashloader.def.mixin.accessor.WeightedBakedModelAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.render.model.BuiltinBakedModel;
import net.minecraft.client.render.model.WeightedBakedModel;
import net.oskarstrom.dashloader.core.annotations.Dependencies;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.core.annotations.DashObject;

import java.util.List;

@Data
@DashObject(WeightedBakedModel.class)
@Dependencies({BasicBakedModel.class, BuiltinBakedModel.class})
public record DashWeightedBakedModel(List<DashWeightedModelEntry> models) implements DashModel {
	public DashWeightedBakedModel(WeightedBakedModel model, DashRegistry registry) {
		this(DashHelper.convertCollection(
				((WeightedBakedModelAccessor) model).getModels(),
				entry -> new DashWeightedModelEntry(entry, registry)));
	}

	@Override
	public WeightedBakedModel toUndash(DashExportHandler handler) {
		return new WeightedBakedModel(DashHelper.convertCollection(models, entry -> entry.toUndash(handler)));
	}
}
