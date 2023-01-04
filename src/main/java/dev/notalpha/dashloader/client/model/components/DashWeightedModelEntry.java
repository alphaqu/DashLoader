package dev.notalpha.dashloader.client.model.components;

import dev.notalpha.dashloader.mixin.accessor.WeightedBakedModelEntryAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.collection.Weighted;

public final class DashWeightedModelEntry {
	public final int model;
	public final int weight;

	public DashWeightedModelEntry(int model, int weight) {
		this.model = model;
		this.weight = weight;
	}

	public DashWeightedModelEntry(Weighted.Present<BakedModel> entry, RegistryWriter writer) {
		this(writer.add(entry.getData()), entry.getWeight().getValue());
	}


	public Weighted.Present<BakedModel> export(RegistryReader handler) {
		//noinspection unchecked
		return WeightedBakedModelEntryAccessor.init(handler.get(this.model), Weight.of(this.weight));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashWeightedModelEntry that = (DashWeightedModelEntry) o;

		if (model != that.model) return false;
		return weight == that.weight;
	}

	@Override
	public int hashCode() {
		int result = model;
		result = 31 * result + weight;
		return result;
	}
}
