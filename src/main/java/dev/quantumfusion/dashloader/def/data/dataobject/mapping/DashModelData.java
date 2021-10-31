package dev.quantumfusion.dashloader.def.data.dataobject.mapping;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.common.IntIntList;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.VanillaData;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
public class DashModelData implements Dashable<Map<Identifier, BakedModel>> {
	public final IntIntList models;

	public DashModelData(IntIntList models) {
		this.models = models;
	}

	public DashModelData(VanillaData data, DashRegistryWriter writer) {
		final Map<Identifier, BakedModel> models = data.getModels();
		this.models = new IntIntList(new ArrayList<>(models.size()));
		models.forEach((identifier, bakedModel) -> {
			if (bakedModel != null)
				this.models.put(writer.add(identifier), writer.add(bakedModel));
		});
	}


	public Map<Identifier, BakedModel> export(final DashRegistryReader reader) {
		final HashMap<Identifier, BakedModel> out = new HashMap<>();
		models.forEach((key, value) -> out.put(reader.get(key), reader.get(value)));
		return out;
	}


}
