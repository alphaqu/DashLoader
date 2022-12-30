package dev.quantumfusion.dashloader.minecraft.splash;

import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.api.DashCacheHandler;
import dev.quantumfusion.dashloader.api.option.Option;
import dev.quantumfusion.dashloader.config.ConfigHandler;
import dev.quantumfusion.dashloader.registry.RegistryFactory;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.util.OptionData;
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
