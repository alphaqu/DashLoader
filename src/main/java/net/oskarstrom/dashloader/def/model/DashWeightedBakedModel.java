package net.oskarstrom.dashloader.def.model;

import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.mixin.accessor.WeightedBakedModelAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.render.model.WeightedBakedModel;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.model.components.DashWeightedModelEntry;

import java.util.List;

@DashObject(WeightedBakedModel.class)
public class DashWeightedBakedModel implements DashModel {

	@Serialize(order = 0)
	public final List<DashWeightedModelEntry> models;

	public DashWeightedBakedModel(@Deserialize("models") List<DashWeightedModelEntry> models) {
		this.models = models;
	}

	public DashWeightedBakedModel(WeightedBakedModel model, DashRegistry registry) {
		this.models = DashHelper.convertCollection(
				((WeightedBakedModelAccessor) model).getModels(),
				entry -> new DashWeightedModelEntry(entry, registry));
	}

	@Override
	public WeightedBakedModel toUndash(DashRegistry registry) {
		return new WeightedBakedModel(DashHelper.convertCollection(models, entry -> entry.toUndash(registry)));
	}

	@Override
	public int getStage() {
		return 1;
	}
}
