package dev.notalpha.dashloader.client.splash;

import dev.notalpha.dashloader.Cache;
import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.config.ConfigHandler;
import dev.notalpha.dashloader.api.config.Option;
import dev.notalpha.dashloader.misc.CachingData;
import dev.notalpha.dashloader.registry.RegistryFactory;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.taski.builtin.StepTask;

import java.util.ArrayList;
import java.util.List;


public class SplashModule implements DashModule<SplashModule.Data> {
	public static final CachingData<List<String>> TEXTS = new CachingData<>();

	@Override
	public void reset(Cache cacheManager) {
		TEXTS.reset(cacheManager, new ArrayList<>());
	}

	@Override
	public Data save(RegistryFactory writer, StepTask task) {
		return new Data(TEXTS.get(Cache.Status.SAVE));
	}

	@Override
	public void load(Data mappings, RegistryReader reader, StepTask task) {
		TEXTS.set(Cache.Status.LOAD, mappings.splashList);
	}

	@Override
	public Class<Data> getDataClass() {
		return SplashModule.Data.class;
	}

	@Override
	public boolean isActive() {
		return ConfigHandler.optionActive(Option.CACHE_SPLASH_TEXT);
	}

	@Override
	public float taskWeight() {
		return 1;
	}

	public static final class Data {
		public final List<String> splashList;

		public Data(List<String> splashList) {
			this.splashList = splashList;
		}
	}
}
