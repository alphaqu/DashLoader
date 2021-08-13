package net.oskarstrom.dashloader.def.model.components;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.collection.Weighted;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.mixin.accessor.WeightedBakedModelEntryAccessor;

public class DashWeightedModelEntry {
	@Serialize(order = 0)
	public final int model;

	@Serialize(order = 1)
	public final int weight;

	public DashWeightedModelEntry(@Deserialize("model") int model,
								  @Deserialize("weight") int weight) {
		this.model = model;
		this.weight = weight;
	}

	public DashWeightedModelEntry(Weighted.Present<BakedModel> entry, DashRegistry registry) {
		this.model = registry.models.register(entry.getData());
		weight = entry.getWeight().getValue();
	}

	public Weighted.Present<BakedModel> toUndash(DashRegistry registry) {
		return WeightedBakedModelEntryAccessor.init(registry.models.getObject(model), Weight.of(weight));
	}


}
