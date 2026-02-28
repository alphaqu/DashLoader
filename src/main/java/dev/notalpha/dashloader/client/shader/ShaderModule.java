package dev.notalpha.dashloader.client.shader;

import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.cache.Cache;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.config.Option;
import dev.notalpha.taski.builtin.StepTask;

public class ShaderModule implements DashModule<ShaderModule.Data> {
	@Override
	public void reset(Cache cache) {
	}

	@Override
	public Data save(RegistryWriter factory, StepTask task) {
		return new Data();
	}

	@Override
	public void load(Data data, RegistryReader reader, StepTask task) {
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
	}
}
