package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.mixin.accessor.WeightedBakedModelEntryAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.collection.Weighted;

@Data
public record DashWeightedModelEntry(int model, int weight) {

	public DashWeightedModelEntry(Weighted.Present<BakedModel> entry, DashRegistryWriter writer) {
		this(writer.add(entry.getData()), entry.getWeight().getValue());
	}


	public Weighted.Present<BakedModel> export(DashRegistryReader handler) {
		return WeightedBakedModelEntryAccessor.init(handler.get(model), Weight.of(weight));
	}


}
