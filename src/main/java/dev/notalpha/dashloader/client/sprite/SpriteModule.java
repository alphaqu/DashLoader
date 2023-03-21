package dev.notalpha.dashloader.client.sprite;

import dev.notalpha.dashloader.api.*;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.cache.DashCache;
import dev.notalpha.dashloader.api.cache.DashModule;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.config.Option;
import dev.notalpha.dashloader.api.collection.IntObjectList;
import dev.notalpha.dashloader.api.cache.CachingData;
import dev.quantumfusion.taski.builtin.StepTask;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class SpriteModule implements DashModule<SpriteModule.Data> {
	public final static CachingData<HashMap<Identifier, SpriteLoader.StitchResult>> ATLASES = new CachingData<>();
	public final static CachingData<HashMap<Identifier, Identifier>> ATLAS_IDS = new CachingData<>(CacheStatus.SAVE);

	@Override
	public void reset(DashCache cacheManager) {
		ATLASES.reset(cacheManager, new HashMap<>());
		ATLAS_IDS.reset(cacheManager, new HashMap<>());
	}

	@Override
	public Data save(RegistryWriter writer, StepTask task) {
		var results = new IntObjectList<DashStitchResult>();

		var map = ATLASES.get(CacheStatus.SAVE);
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

		ATLASES.set(CacheStatus.LOAD, stitchResults);
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
