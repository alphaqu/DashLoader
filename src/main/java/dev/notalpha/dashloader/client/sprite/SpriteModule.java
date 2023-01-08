package dev.notalpha.dashloader.client.sprite;

import dev.notalpha.dashloader.Cache;
import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.config.ConfigHandler;
import dev.notalpha.dashloader.api.config.Option;
import dev.notalpha.dashloader.misc.OptionData;
import dev.notalpha.dashloader.misc.duck.SpriteAtlasTextureDuck;
import dev.notalpha.dashloader.registry.RegistryFactory;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.quantumfusion.taski.builtin.StepTask;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpriteModule implements DashModule<SpriteModule.Data> {
	public final static OptionData<Map<Identifier, AtlasData>> ATLASES = new OptionData<>();

	@Override
	public void reset(Cache cacheManager) {
		ATLASES.reset(cacheManager, new HashMap<>());
	}

	@Override
	public Data save(RegistryFactory writer, StepTask task) {
		var map = ATLASES.get(Cache.Status.SAVE);
		var results = new ArrayList<Integer>();
		task.doForEach(map, (identifier, data) -> {
			results.add(writer.add(data));
		});

		return new Data(results);
	}

	@Override
	public void load(Data mappings, RegistryReader reader, StepTask task) {
		Map<Identifier, AtlasData> out = new HashMap<>();
		for (Integer atlas : mappings.results) {
			AtlasData r = reader.get(atlas);
			out.put(r.id, r);
		}
		ATLASES.set(Cache.Status.LOAD, out);
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
		public final List<Integer> results;

		public Data(List<Integer> results) {
			this.results = results;
		}
	}
}
