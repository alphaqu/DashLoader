package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.mixin.accessor.WeightedBakedModelEntryAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.collection.Weighted;

@Data
public record DashWeightedModelEntry(int model, int weight) {

	public DashWeightedModelEntry(Weighted.Present<BakedModel> entry, RegistryWriter writer) {
		this(writer.add(entry.getData()), entry.getWeight().getValue());
	}


	public Weighted.Present<BakedModel> export(RegistryReader handler) {
		return WeightedBakedModelEntryAccessor.init(handler.get(model), Weight.of(weight));
	}
}
