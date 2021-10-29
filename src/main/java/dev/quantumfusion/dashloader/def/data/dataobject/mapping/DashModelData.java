package dev.quantumfusion.dashloader.def.data.dataobject.mapping;

import dev.quantumfusion.dashloader.def.DashLoader;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.data.Pointer2PointerMap;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import dev.quantumfusion.dashloader.def.data.VanillaData;

import java.util.HashMap;
import java.util.Map;

public class DashModelData implements Dashable<Map<Identifier, BakedModel>> {


	@Serialize(order = 0)
	public final Pointer2PointerMap models;


	public DashModelData(@Deserialize("models") Pointer2PointerMap models) {
		this.models = models;
	}

	public DashModelData(VanillaData data, DashRegistry registry, DashLoader.TaskHandler taskHandler) {
		final Map<Identifier, BakedModel> models = data.getModels();
		final int size = models.size();
		this.models = new Pointer2PointerMap(size);
		taskHandler.setSubtasks(size);
		models.forEach((identifier, bakedModel) -> {
			if (bakedModel != null) {
				this.models.add(Pointer2PointerMap.Entry.of(registry.add(identifier), registry.add(bakedModel)));
			}
			taskHandler.completedSubTask();
		});
	}


	public Map<Identifier, BakedModel> toUndash(final DashRegistry registry) {
		final HashMap<Identifier, BakedModel> out = new HashMap<>();
		models.forEach((entry) -> out.put(registry.get(entry.key), registry.get(entry.value)));
		return out;
	}


}
