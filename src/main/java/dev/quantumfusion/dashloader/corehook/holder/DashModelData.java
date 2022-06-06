package dev.quantumfusion.dashloader.corehook.holder;

import dev.quantumfusion.dashloader.DashDataManager;
import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.common.IntIntList;
import dev.quantumfusion.dashloader.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.data.model.DashModel;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.taski.builtin.StepTask;
import net.minecraft.block.Block;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DashModelData implements Dashable<Map<Identifier, BakedModel>> {
	public final IntIntList models; // identifier to model list

	public DashModelData(IntIntList models) {
		this.models = models;
	}

	public DashModelData(DashDataManager data, RegistryWriter writer, StepTask parent) {
		var writeContextData = data.getWriteContextData();
		var missingModelsWrite = writeContextData.missingModelsWrite;
		var models = data.bakedModels.getMinecraftData();

		this.models = new IntIntList(new ArrayList<>(models.size()));
		parent.run(new StepTask("Models", models.size()), (task) -> {
			var modelChunk = writer.getChunk(DashModel.class);
			var identifierChunk = writer.getChunk(DashIdentifierInterface.class);
			models.forEach((identifier, bakedModel) -> {
				if (bakedModel != null) {
					final int add = writer.addDirect(modelChunk, bakedModel);
					if (!missingModelsWrite.containsKey(bakedModel)) {
						this.models.put(writer.addDirect(identifierChunk, identifier), add);
					}
				}
				task.next();
			});
		});
	}

	public Map<Identifier, BakedModel> export(final RegistryReader reader) {
		final HashMap<Identifier, BakedModel> out = new HashMap<>();
		this.models.forEach((key, value) -> out.put(reader.get(key), reader.get(value)));

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
		DashLoader.INSTANCE.thread.parallelRunnable(tasks);
		DashLoader.LOGGER.info("Found {} Missing BlockState Models", missingModelsRead.size());
		return out;
	}


}
