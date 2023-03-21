package dev.notalpha.dashloader.client.splash;

import dev.notalpha.dashloader.api.*;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.cache.DashCache;
import dev.notalpha.dashloader.api.cache.DashModule;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.config.Option;
import dev.notalpha.dashloader.api.cache.CachingData;
import dev.quantumfusion.taski.builtin.StepTask;

import java.util.ArrayList;
import java.util.List;


public class SplashModule implements DashModule<SplashModule.Data> {
	public static final CachingData<List<String>> TEXTS = new CachingData<>();

	@Override
	public void reset(DashCache cacheManager) {
		TEXTS.reset(cacheManager, new ArrayList<>());
	}

	@Override
	public Data save(RegistryWriter writer, StepTask task) {
		return new Data(TEXTS.get(CacheStatus.SAVE));
	}

	@Override
	public void load(Data mappings, RegistryReader reader, StepTask task) {
		TEXTS.set(CacheStatus.LOAD, mappings.splashList);
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
