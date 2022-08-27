package dev.quantumfusion.dashloader.data.model;

import dev.quantumfusion.dashloader.api.DashDependencies;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.data.model.components.DashWeightedModelEntry;
import dev.quantumfusion.dashloader.mixin.accessor.WeightedBakedModelAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.WeightedBakedModel;
import net.minecraft.util.collection.Weighted.Present;

@DashObject(WeightedBakedModel.class)
@DashDependencies({DashBasicBakedModel.class, DashBuiltinBakedModel.class})
public final class DashWeightedBakedModel implements DashModel {
	public final List<DashWeightedModelEntry> models;

	public DashWeightedBakedModel(List<DashWeightedModelEntry> models) {
		this.models = models;
	}

	public DashWeightedBakedModel(WeightedBakedModel model, RegistryWriter writer) {
		this.models = new ArrayList<>();
		for (var weightedModel : ((WeightedBakedModelAccessor) model).getModels()) {
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
