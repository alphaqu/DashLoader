package dev.quantumfusion.dashloader.data.mapping;

import dev.quantumfusion.dashloader.DashDataManager;
import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.data.common.IntIntList;
import dev.quantumfusion.dashloader.registry.RegistryFactory;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.taski.builtin.StepTask;
import net.minecraft.block.Block;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static dev.quantumfusion.dashloader.DashLoader.DL;


public class DashModelData implements Dashable<Map<Identifier, BakedModel>> {
	public final IntIntList models; // identifier to model list

	public DashModelData(IntIntList models) {
		this.models = models;
	}

	public DashModelData(DashDataManager data, RegistryFactory writer, StepTask task) {
		var writeContextData = data.getWriteContextData();
		var missingModelsWrite = writeContextData.missingModelsWrite;
		var models = data.bakedModels.getMinecraftData();

		this.models = new IntIntList(new ArrayList<>(models.size()));
		task.doForEach(models, (identifier, bakedModel) -> {
			if (bakedModel != null) {
				final int add = writer.add(bakedModel);
				if (!missingModelsWrite.containsKey(bakedModel)) {
					this.models.put(writer.add(identifier), add);
				}
			}
		});
	}

	public Map<Identifier, BakedModel> export(final RegistryReader reader) {
		final HashMap<Identifier, BakedModel> out = new HashMap<>();
		this.models.forEach((key, value) -> out.put(reader.get(key), reader.get(value)));

		var missingModelsRead = DL.getData().getReadContextData().missingModelsRead;
		var tasks = new ArrayList<Runnable>();
		DashLoader.LOG.info("Scanning Blocks");
		for (Block block : Registries.BLOCK) {
			tasks.add(() -> block.getStateManager().getStates().forEach((blockState) -> {
				final ModelIdentifier modelId = BlockModels.getModelId(blockState);
				if (!out.containsKey(modelId)) {
					missingModelsRead.put(blockState, modelId);
				}
			}));
		}

		DashLoader.LOG.info("Verifying {} BlockStates", tasks.size());
		DL.thread.parallelRunnable(tasks);
		DashLoader.LOG.info("Found {} Missing BlockState Models", missingModelsRead.size());
		return out;
	}


}
