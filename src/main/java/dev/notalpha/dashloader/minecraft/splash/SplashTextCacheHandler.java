package dev.notalpha.dashloader.minecraft.splash;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.util.OptionData;
import dev.notalpha.dashloader.api.DashCacheHandler;
import dev.notalpha.dashloader.api.option.Option;
import dev.notalpha.dashloader.registry.RegistryFactory;
import dev.quantumfusion.taski.builtin.StepTask;

import java.util.ArrayList;
import java.util.List;


public class SplashTextCacheHandler implements DashCacheHandler<SplashTextCacheHandler.Data> {
	public static final OptionData<List<String>> TEXTS = new OptionData<>();

	@Override
	public void reset(DashLoader.Status status) {
		TEXTS.set(status, new ArrayList<>());
	}

	@Override
	public Data saveMappings(RegistryFactory writer, StepTask task) {
		return new Data(TEXTS.get(DashLoader.Status.SAVE));
	}

	@Override
	public void loadMappings(Data mappings, RegistryReader reader, StepTask task) {
		TEXTS.set(DashLoader.Status.LOAD, mappings.splashList);
	}

	@Override
	public Class<Data> getDataClass() {
		return SplashTextCacheHandler.Data.class;
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
