package dev.quantumfusion.dashloader.def.corehook.holder;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.common.IntIntList;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.Identifier;

import java.util.*;


@Data
public class DashModelData implements Dashable<Map<Identifier, BakedModel>> {
	public final IntIntList models;
	public final List<Integer> missingModels;

	public DashModelData(IntIntList models, List<Integer> missingModels) {
		this.models = models;
		this.missingModels = missingModels;
	}

	public DashModelData(DashDataManager data, DashRegistryWriter writer) {
		final Map<Identifier, BakedModel> models = data.bakedModels.getMinecraftData();
		this.models = new IntIntList(new ArrayList<>(models.size()));
		models.forEach((identifier, bakedModel) -> {
			if (bakedModel != null)
				this.models.put(writer.add(identifier), writer.add(bakedModel));
		});

		this.missingModels = new ArrayList<>();

		var writeContextData = DashLoader.getData().getWriteContextData();
		var flippedModelMap = new IdentityHashMap<BakedModel, Identifier>();
		models.forEach((identifier, bakedModel) -> flippedModelMap.put(bakedModel, identifier));

		writeContextData.missingModelsWrite.forEach((bakedModel, missingDashModel) -> {
			final Identifier object = flippedModelMap.get(bakedModel);
			if (object == null) {
				throw new RuntimeException("what");
			}
			missingModels.add(writer.add(object));
		});
	}


	public Map<Identifier, BakedModel> export(final DashRegistryReader reader) {
		final HashMap<Identifier, BakedModel> out = new HashMap<>();
		models.forEach((key, value) -> out.put(reader.get(key), reader.get(value)));

		for (Integer missingModel : missingModels) {
			DashLoader.getData().getReadContextData().missingModelsRead.add(reader.get(missingModel));
		}
		return out;
	}


}
