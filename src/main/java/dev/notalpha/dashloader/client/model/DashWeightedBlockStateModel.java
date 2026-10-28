package dev.notalpha.dashloader.client.model;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.client.Dazy;
import dev.notalpha.dashloader.mixin.accessor.WeightedBlockStateModelAccessor;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.ErrorCollectingSpriteGetter;
import net.minecraft.client.render.model.WeightedBlockStateModel;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weighted;

import java.util.ArrayList;
import java.util.List;

/**
 * Cached form of a {@link WeightedBlockStateModel} (1.21.5+ model system).
 * All variants are stored so vanilla random picking ({@link Pool}) behaves
 * exactly like an uncached load. Rebuilt as a real {@link Pool} on LOAD.
 */
public final class DashWeightedBlockStateModel implements DashObject<WeightedBlockStateModel, DashWeightedBlockStateModel.DazyImpl> {
	public final List<Entry> entries;

	public DashWeightedBlockStateModel(List<Entry> entries) {
		this.entries = entries;
	}

	public DashWeightedBlockStateModel(WeightedBlockStateModel model, RegistryWriter writer) {
		this.entries = new ArrayList<>();
		for (Weighted<BlockStateModel> entry : ((WeightedBlockStateModelAccessor) model).getModels().getEntries()) {
			this.entries.add(new Entry(entry.weight(), writer.add(entry.value())));
		}
	}

	@Override
	public DazyImpl export(RegistryReader reader) {
		List<DazyImpl.Entry> entriesOut = new ArrayList<>(this.entries.size());
		for (Entry entry : this.entries) {
			entriesOut.add(new DazyImpl.Entry(entry.weight, reader.get(entry.model)));
		}
		return new DazyImpl(entriesOut);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashWeightedBlockStateModel that = (DashWeightedBlockStateModel) o;

		return entries.equals(that.entries);
	}

	@Override
	public int hashCode() {
		return entries.hashCode();
	}

	public static final class Entry {
		public final int weight;
		public final int model;

		public Entry(int weight, int model) {
			this.weight = weight;
			this.model = model;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Entry entry = (Entry) o;

			if (weight != entry.weight) return false;
			return model == entry.model;
		}

		@Override
		public int hashCode() {
			int result = weight;
			result = 31 * result + model;
			return result;
		}
	}

	public static class DazyImpl extends Dazy<WeightedBlockStateModel> {
		public final List<Entry> entries;

		public DazyImpl(List<Entry> entries) {
			this.entries = entries;
		}

		@Override
		protected WeightedBlockStateModel resolve(ErrorCollectingSpriteGetter spriteLoader) {
			Pool.Builder<BlockStateModel> pool = Pool.builder();
			for (Entry entry : this.entries) {
				pool.add(entry.model.get(spriteLoader), entry.weight);
			}
			return new WeightedBlockStateModel(pool.build());
		}

		public static class Entry {
			public final int weight;
			public final DashBlockStateModel.DazyImpl model;

			public Entry(int weight, DashBlockStateModel.DazyImpl model) {
				this.weight = weight;
				this.model = model;
			}
		}
	}
}
