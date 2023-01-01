package dev.notalpha.dashloader.minecraft.shader;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.util.OptionData;
import dev.notalpha.dashloader.api.DashCacheHandler;
import dev.notalpha.dashloader.api.option.Option;
import dev.notalpha.dashloader.io.data.collection.ObjectIntList;
import dev.notalpha.dashloader.registry.RegistryFactory;
import dev.quantumfusion.taski.builtin.StepTask;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.gl.ShaderProgram;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShaderCacheHandler implements DashCacheHandler<ShaderCacheHandler.Data> {
	public static final OptionData<HashMap<String, ShaderProgram>> SHADERS = new OptionData<>();
	public static final OptionData<Int2ObjectMap<List<String>>> WRITE_PROGRAM_SOURCES = new OptionData<>(DashLoader.Status.SAVE);

	@Override
	public void reset(DashLoader.Status status) {
		SHADERS.set(status, new HashMap<>());
		WRITE_PROGRAM_SOURCES.set(status, new Int2ObjectOpenHashMap<>());
	}

	@Override
	public Data saveMappings(RegistryFactory writer, StepTask task) {
		final Map<String, ShaderProgram> minecraftData = SHADERS.get(DashLoader.Status.SAVE);
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
	public void loadMappings(Data mappings, RegistryReader reader, StepTask task) {
		HashMap<String, ShaderProgram> out = new HashMap<>();
		mappings.shaders.forEach((key, value) -> out.put(key, reader.get(value)));
		SHADERS.set(DashLoader.Status.LOAD, out);
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
