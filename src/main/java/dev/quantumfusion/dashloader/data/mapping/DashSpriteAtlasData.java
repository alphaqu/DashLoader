package dev.quantumfusion.dashloader.data.mapping;

import dev.quantumfusion.dashloader.DashDataManager;
import dev.quantumfusion.dashloader.data.common.IntObjectList;
import dev.quantumfusion.dashloader.data.image.DashStitchResult;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.taski.builtin.StepTask;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.util.Identifier;

public class DashSpriteAtlasData {
	public final IntObjectList<DashStitchResult> results;

	public DashSpriteAtlasData(IntObjectList<DashStitchResult> results) {
		this.results = results;
	}

	public DashSpriteAtlasData(DashDataManager data, RegistryWriter writer, StepTask parent) {
		this.results = new IntObjectList<>();
		var results = data.getWriteContextData().stitchResults;
		parent.run(new StepTask("Atlases", results.size()), stepTask -> {
			results.forEach((identifier, stitchResult) -> {
				stepTask.run(new StepTask("Images", stitchResult.regions().size()), stepTask1 -> {
					this.results.put(writer.add(identifier), new DashStitchResult(stitchResult, writer, stepTask1));
				});
			});
		});
	}

	public void export(DashDataManager data, RegistryReader reader) {
		Object2ObjectMap<Identifier, SpriteLoader.StitchResult> stitchResults = data.getReadContextData().stitchResults;

		this.results.forEach((identifier, stitchResult) -> {
			stitchResults.put(reader.get(identifier), stitchResult.export(reader));
		});
	}
}
