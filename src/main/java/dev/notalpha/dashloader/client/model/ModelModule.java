package dev.notalpha.dashloader.client.model;

import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.cache.Cache;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.config.Option;
import dev.notalpha.taski.builtin.StepTask;

public class ModelModule implements DashModule<ModelModule.Data> {
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
	public float taskWeight() {
		return 1000;
	}

	@Override
	public boolean isActive() {
		return ConfigHandler.optionActive(Option.CACHE_MODEL_LOADER);
	}

	public static final class Data {
	}
}
