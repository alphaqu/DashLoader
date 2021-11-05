package dev.quantumfusion.dashloader.def.corehook.holder;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.common.IntIntList;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.core.util.DashThreading;
import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.def.data.model.DashModel;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.block.Block;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@Data
public class DashModelData implements Dashable<Map<Identifier, BakedModel>> {
	public final IntIntList models; // identifier to model list

	public DashModelData(IntIntList models) {
		this.models = models;
	}

	public DashModelData(DashDataManager data, DashRegistryWriter writer) {
		var writeContextData = data.getWriteContextData();
		var missingModelsWrite = writeContextData.missingModelsWrite;
		var models = data.bakedModels.getMinecraftData();

		this.models = new IntIntList(new ArrayList<>(models.size()));

		var modelChunk = writer.getChunk(DashModel.class);
		var identifierChunk = writer.getChunk(DashIdentifierInterface.class);
		models.forEach((identifier, bakedModel) -> {
			if (bakedModel != null) {
				final int add = writer.addDirect(modelChunk, bakedModel);
				if (!missingModelsWrite.containsKey(bakedModel)) {
					this.models.put(writer.addDirect(identifierChunk, identifier), add);
				}
			}
		});
	}

	public Map<Identifier, BakedModel> export(final DashRegistryReader reader) {
		final HashMap<Identifier, BakedModel> out = new HashMap<>();
		models.forEach((key, value) -> out.put(reader.get(key), reader.get(value)));

		var missingModelsRead = DashLoader.getData().getReadContextData().missingModelsRead;
		var tasks = new ArrayList<Runnable>();
		DashLoader.LOGGER.info("Scanning Blocks");
		for (Block block : Registry.BLOCK) {
			tasks.add(() -> block.getStateManager().getStates().forEach((blockState) -> {
				final ModelIdentifier modelId = BlockModels.getModelId(blockState);
				if (!out.containsKey(modelId)) {
					missingModelsRead.put(blockState, modelId);
				}
			}));
		}

		DashLoader.LOGGER.info("Verifying {} BlockStates", tasks.size());
		DashThreading.run(tasks);
		DashLoader.LOGGER.info("Found {} Missing BlockState Models", missingModelsRead.size());
		return out;
	}


}
