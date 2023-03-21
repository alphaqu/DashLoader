package dev.notalpha.dashloader.client.shader;

import dev.notalpha.dashloader.api.*;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.cache.DashCache;
import dev.notalpha.dashloader.api.cache.DashModule;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.config.Option;
import dev.notalpha.dashloader.api.collection.ObjectIntList;
import dev.notalpha.dashloader.api.cache.CachingData;
import dev.quantumfusion.taski.builtin.StepTask;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.gl.ShaderProgram;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShaderModule implements DashModule<ShaderModule.Data> {
	public static final CachingData<HashMap<String, ShaderProgram>> SHADERS = new CachingData<>();
	public static final CachingData<Int2ObjectMap<List<String>>> WRITE_PROGRAM_SOURCES = new CachingData<>(CacheStatus.SAVE);

	@Override
	public void reset(DashCache cacheManager) {
		SHADERS.reset(cacheManager, new HashMap<>());
		WRITE_PROGRAM_SOURCES.reset(cacheManager, new Int2ObjectOpenHashMap<>());
	}

	@Override
	public Data save(RegistryWriter writer, StepTask task) {
		final Map<String, ShaderProgram> minecraftData = SHADERS.get(CacheStatus.SAVE);
		if (minecraftData == null) {
			return null;
		}

		var shaders = new ObjectIntList<String>();
		task.doForEach(minecraftData, (s, shader) -> {
			shaders.put(s, writer.add(shader));
		});

		return new Data(shaders);
	}

	@Override
	public void load(Data mappings, RegistryReader reader, StepTask task) {
		HashMap<String, ShaderProgram> out = new HashMap<>();
		mappings.shaders.forEach((key, value) -> out.put(key, reader.get(value)));
		SHADERS.set(CacheStatus.LOAD, out);
	}

	@Override
	public Class<Data> getDataClass() {
		return Data.class;
	}

	@Override
	public boolean isActive() {
		return ConfigHandler.optionActive(Option.CACHE_SHADER);
	}

	public static final class Data {
		public final ObjectIntList<String> shaders;

		public Data(ObjectIntList<String> shaders) {
			this.shaders = shaders;
		}
	}
}
