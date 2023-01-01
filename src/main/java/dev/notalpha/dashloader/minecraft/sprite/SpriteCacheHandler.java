package dev.notalpha.dashloader.minecraft.sprite;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.DashCacheHandler;
import dev.notalpha.dashloader.api.Option;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.io.data.collection.IntObjectList;
import dev.notalpha.dashloader.registry.RegistryFactory;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.util.OptionData;
import dev.quantumfusion.taski.builtin.StepTask;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class SpriteCacheHandler implements DashCacheHandler<SpriteCacheHandler.Data> {
	public final static OptionData<HashMap<Identifier, SpriteLoader.StitchResult>> ATLASES = new OptionData<>();
	@Override
	public void reset(DashLoader.Status status) {
		ATLASES.set(status, new HashMap<>());
	}

	@Override
	public Data saveMappings(RegistryFactory writer, StepTask task) {
		var results = new IntObjectList<DashStitchResult>();

		var map = ATLASES.get(DashLoader.Status.SAVE);
		task.doForEach(map, (identifier, stitchResult) -> {
			StepTask atlases = new StepTask("atlas", stitchResult.regions().size());
			task.setSubTask(atlases);
			results.put(writer.add(identifier), new DashStitchResult(stitchResult, writer, atlases));
		});

		return new Data(results);
	}

	@Override
	public void loadMappings(Data mappings, RegistryReader reader, StepTask task) {
		HashMap<Identifier, SpriteLoader.StitchResult> stitchResults = new HashMap<>(mappings.results.list().size());
		mappings.results.forEach((identifier, stitchResult) -> {
			stitchResults.put(reader.get(identifier), stitchResult.export(reader));
		});

		ATLASES.set(DashLoader.Status.LOAD, stitchResults);
	}

	@Override
	public Class<Data> getDataClass() {
		return Data.class;
	}

	@Override
	public boolean isActive() {
		return ConfigHandler.optionActive(Option.CACHE_SPRITES);
	}

	public static final class Data {
		public final IntObjectList<DashStitchResult> results;
		public Data(IntObjectList<DashStitchResult> results) {
			this.results = results;
		}
	}
}
