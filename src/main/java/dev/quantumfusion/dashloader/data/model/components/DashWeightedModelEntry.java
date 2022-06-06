package dev.quantumfusion.dashloader.data.model.components;

import dev.quantumfusion.dashloader.mixin.accessor.WeightedBakedModelEntryAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.collection.Weighted;

public record DashWeightedModelEntry(int model, int weight) {

	public DashWeightedModelEntry(Weighted.Present<BakedModel> entry, RegistryWriter writer) {
		this(writer.add(entry.getData()), entry.getWeight().getValue());
	}


	public Weighted.Present<BakedModel> export(RegistryReader handler) {
		return WeightedBakedModelEntryAccessor.init(handler.get(this.model), Weight.of(this.weight));
	}
}
