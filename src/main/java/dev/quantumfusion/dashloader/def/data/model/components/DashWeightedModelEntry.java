package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.collection.Weighted;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import dev.quantumfusion.dashloader.def.mixin.accessor.WeightedBakedModelEntryAccessor;

@Data
public record DashWeightedModelEntry(int model, int weight) {

	public DashWeightedModelEntry(Weighted.Present<BakedModel> entry, DashRegistry registry) {
		this(registry.add(entry.getData()), entry.getWeight().getValue());
	}


	public Weighted.Present<BakedModel> toUndash(DashExportHandler handler) {
		return WeightedBakedModelEntryAccessor.init(handler.get(model), Weight.of(weight));
	}


}
