package dev.notalpha.dashloader.misc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;

public class TranslationHelper {
	private static final TranslationHelper INSTANCE = new TranslationHelper();
	// Read from the caching thread (DashToastState.setOverwriteText) while the client thread can rewrite it on a language change. ConcurrentHashMap, because a HashMap resize racing a get can spin forever on HotSpot.
	private final ConcurrentHashMap<String, String> translations;
	private volatile String langCode;

	private TranslationHelper() {
		this.translations = new ConcurrentHashMap<>();
	}

	public static TranslationHelper getInstance() {
		var langCode = Minecraft.getInstance().getLanguageManager().getSelected();
		if (!Objects.equals(INSTANCE.langCode, langCode)) {
			INSTANCE.langCode = langCode;
			INSTANCE.loadLang(langCode);
		}
		return INSTANCE;
	}

	private void loadLang(String langCode) {
		this.langCode = langCode;
		var stream = this.getClass().getClassLoader().getResourceAsStream("dashloader/lang/" + langCode + ".json");
		if (stream != null) {
			Language.loadFromJson(stream, this.translations::put);
		} else {
			stream = this.getClass().getClassLoader().getResourceAsStream("dashloader/lang/en_us.json");
			if (stream != null) {
				Language.loadFromJson(stream, this.translations::put);
			}
		}
	}

	public String get(String text) {
		return this.translations.getOrDefault(text, text);
	}

	public boolean has(String key) {
		return this.translations.containsKey(key);
	}
}



