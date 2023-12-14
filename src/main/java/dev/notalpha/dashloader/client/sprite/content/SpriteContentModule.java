package dev.notalpha.dashloader.client.sprite.content;

import dev.notalpha.dashloader.api.CachingData;
import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.cache.Cache;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.collection.IntIntList;
import dev.notalpha.dashloader.api.collection.IntObjectList;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.taski.builtin.StepTask;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.Identifier;

import java.util.*;

public class SpriteContentModule implements DashModule<SpriteContentModule.Data>  {
	public final static CachingData<Map<Identifier, DashAtlasData>> ATLASES = new CachingData<>();
	@Override
	public void reset(Cache cache) {
		ATLASES.reset(cache, new HashMap<>());
	}

	@Override
	public Data save(RegistryWriter writer, StepTask task) {
		var atlases = ATLASES.get(CacheStatus.SAVE);
		assert atlases != null;


		var map = new IntObjectList<IntIntList>();
		task.doForEach(atlases, (atlasId, atlasData) -> {
			IntIntList sprites = new IntIntList();
			atlasData.sprites.forEach((id, spriteContents) -> {
				sprites.put(writer.add(id), writer.add(spriteContents));
			});
			map.put(writer.add(atlasId), sprites);
		});

		return new Data(map);
	}

	@Override
	public void load(Data data, RegistryReader reader, StepTask task) {
		var atlases = ATLASES.get(CacheStatus.LOAD);
		assert atlases != null;

		data.atlases.forEach((atlasId, atlasData) -> {
			Identifier atlasIdentifier = reader.get(atlasId);
			Map<Identifier, SpriteContents> sprites = new HashMap<>();
			atlasData.forEach((key, value) -> {
				Identifier identifier = reader.get(key);
				SpriteContents contents = reader.get(value);
				sprites.put(identifier, contents);
			});
			atlases.put(atlasIdentifier, new DashAtlasData(atlasIdentifier, sprites));
		});
	}

	@Override
	public Class<Data> getDataClass() {
		return Data.class;
	}


	public static class Data {
		public final IntObjectList<IntIntList> atlases;

		public Data(IntObjectList<IntIntList> atlases) {
			this.atlases = atlases;
		}
	}
}
