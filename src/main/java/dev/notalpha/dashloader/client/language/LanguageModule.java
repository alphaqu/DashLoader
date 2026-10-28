package dev.notalpha.dashloader.client.language;

import dev.notalpha.dashloader.api.CachingData;
import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.cache.Cache;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.collection.ObjectObjectList;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.config.Option;
import dev.notalpha.taski.builtin.StepTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageModule implements DashModule<LanguageModule.Data> {
	public static final CachingData<ObjectObjectList<String, Translations>> SAVE =
			new CachingData<>(CacheStatus.SAVE);
	public static final CachingData<ObjectObjectList<String, Translations>> LOAD =
			new CachingData<>(CacheStatus.LOAD);

	public static String key(List<String> codes) {
		return String.join(",", codes);
	}

	public static Translations getLoad(List<String> codes) {
		return find(LOAD.get(CacheStatus.LOAD), codes);
	}

	public static Translations find(ObjectObjectList<String, Translations> languages, List<String> codes) {
		if (languages == null) {
			return null;
		}
		String wanted = key(codes);
		for (ObjectObjectList.ObjectObjectEntry<String, Translations> entry : languages.list()) {
			if (entry.key().equals(wanted)) {
				return entry.value();
			}
		}
		return null;
	}

	@Override
	public void reset(Cache cache) {
		SAVE.reset(cache, new ObjectObjectList<>());
		LOAD.reset(cache, new ObjectObjectList<>());
	}

	@Override
	public Data save(RegistryWriter writer, StepTask task) {
		ObjectObjectList<String, Translations> staged = SAVE.get(CacheStatus.SAVE);
		return new Data(staged == null ? new ObjectObjectList<>() : staged);
	}

	@Override
	public void load(Data data, RegistryReader reader, StepTask task) {
		if (data == null || data.languages == null) {
			return;
		}
		LOAD.set(CacheStatus.LOAD, data.languages);
	}

	@Override
	public Class<Data> getDataClass() {
		return Data.class;
	}

	@Override
	public boolean isActive() {
		return ConfigHandler.optionActive(Option.CACHE_LANGUAGE);
	}

	@Override
	public float taskWeight() {
		return 10;
	}

	public static final class Data {
		public final ObjectObjectList<String, Translations> languages;

		public Data(ObjectObjectList<String, Translations> languages) {
			this.languages = languages;
		}
	}

	public static final class Translations {
		public final ObjectObjectList<String, String> entries;
		public final boolean rightToLeft;

		public Translations(ObjectObjectList<String, String> entries, boolean rightToLeft) {
			this.entries = entries;
			this.rightToLeft = rightToLeft;
		}

		public Map<String, String> asMap() {
			Map<String, String> out = new HashMap<>(entries.list().size());
			for (ObjectObjectList.ObjectObjectEntry<String, String> entry : entries.list()) {
				out.put(entry.key(), entry.value());
			}
			return Map.copyOf(out);
		}
	}
}