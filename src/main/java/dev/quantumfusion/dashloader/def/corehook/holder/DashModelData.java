package dev.quantumfusion.dashloader.def.corehook.holder;

import dev.quantumfusion.dashloader.core.DashLoaderCore;
import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.common.IntIntList;
import dev.quantumfusion.dashloader.core.progress.task.CountTask;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.core.registry.chunk.write.AbstractWriteChunk;
import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.DashDataManager.DashWriteContextData;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.def.data.model.DashModel;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
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

	public DashModelData(DashDataManager data, RegistryWriter writer) {
		var writeContextData = data.getWriteContextData();
		var missingModelsWrite = writeContextData.missingModelsWrite;
		var models = data.bakedModels.getMinecraftData();

		this.models = new IntIntList(new ArrayList<>(models.size()));

		CountTask task = new CountTask(models.size());
		DashLoaderCore.PROGRESS.getCurrentContext().setSubtask(task);
		var modelChunk = writer.getChunk(DashModel.class);
		var identifierChunk = writer.getChunk(DashIdentifierInterface.class);
		models.forEach((identifier, bakedModel) -> {
			if (bakedModel != null) {
				final int add = writer.addDirect(modelChunk, bakedModel);
				if (!missingModelsWrite.containsKey(bakedModel)) {
					this.models.put(writer.addDirect(identifierChunk, identifier), add);
				}
			}
			task.completedTask();
		});
	}

	public Map<Identifier, BakedModel> export(final RegistryReader reader) {
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
		DashLoaderCore.THREAD.parallelRunnable(tasks);
		DashLoader.LOGGER.info("Found {} Missing BlockState Models", missingModelsRead.size());
		return out;
	}


}
