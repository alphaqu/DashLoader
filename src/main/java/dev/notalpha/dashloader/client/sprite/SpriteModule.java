package dev.notalpha.dashloader.client.sprite;

import dev.notalpha.dashloader.Cache;
import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.config.ConfigHandler;
import dev.notalpha.dashloader.api.config.Option;
import dev.notalpha.dashloader.io.data.collection.IntObjectList;
import dev.notalpha.dashloader.misc.CachingData;
import dev.notalpha.dashloader.registry.RegistryFactory;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.taski.builtin.StepTask;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class SpriteModule implements DashModule<SpriteModule.Data> {
	public final static CachingData<HashMap<Identifier, SpriteLoader.StitchResult>> ATLASES = new CachingData<>();
	public final static CachingData<HashMap<Identifier, Identifier>> ATLAS_IDS = new CachingData<>(Cache.Status.SAVE);

	@Override
	public void reset(Cache cacheManager) {
		ATLASES.reset(cacheManager, new HashMap<>());
		ATLAS_IDS.reset(cacheManager, new HashMap<>());
	}

	@Override
	public Data save(RegistryFactory writer, StepTask task) {
		var results = new IntObjectList<DashStitchResult>();

		var map = ATLASES.get(Cache.Status.SAVE);
		task.doForEach(map, (identifier, stitchResult) -> {
			StepTask atlases = new StepTask("atlas", stitchResult.regions().size());
			task.setSubTask(atlases);
			results.put(writer.add(identifier), new DashStitchResult(stitchResult, writer, atlases));
		});

		return new Data(results);
	}

	@Override
	public void load(Data mappings, RegistryReader reader, StepTask task) {
		HashMap<Identifier, SpriteLoader.StitchResult> stitchResults = new HashMap<>(mappings.results.list().size());
		mappings.results.forEach((identifier, stitchResult) -> {
			stitchResults.put(reader.get(identifier), stitchResult.export(reader));
		});

		ATLASES.set(Cache.Status.LOAD, stitchResults);
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
