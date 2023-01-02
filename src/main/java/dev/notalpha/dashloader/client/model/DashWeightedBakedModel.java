package dev.notalpha.dashloader.client.model;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.client.model.components.DashWeightedModelEntry;
import dev.notalpha.dashloader.mixin.accessor.WeightedBakedModelAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.WeightedBakedModel;
import net.minecraft.util.collection.Weighted.Present;

import java.util.ArrayList;
import java.util.List;

public final class DashWeightedBakedModel implements DashObject<WeightedBakedModel> {
	public final List<DashWeightedModelEntry> models;

	public DashWeightedBakedModel(List<DashWeightedModelEntry> models) {
		this.models = models;
	}

	public DashWeightedBakedModel(WeightedBakedModel model, RegistryWriter writer) {
		this.models = new ArrayList<>();
		for (var weightedModel : ((WeightedBakedModelAccessor) model).getBakedModels()) {
			this.models.add(new DashWeightedModelEntry(weightedModel, writer));
		}
	}

	@Override
	public WeightedBakedModel export(RegistryReader reader) {
		var modelsOut = new ArrayList<Present<BakedModel>>();
		for (DashWeightedModelEntry model : this.models) {
			modelsOut.add(model.export(reader));
		}
		return new WeightedBakedModel(modelsOut);
	}
}
