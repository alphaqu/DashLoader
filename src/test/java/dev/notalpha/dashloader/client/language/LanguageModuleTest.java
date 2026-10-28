package dev.notalpha.dashloader.client.language;

import dev.notalpha.dashloader.api.collection.ObjectObjectList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageModuleTest {
	@Test
	void cacheKeyIsDistinguishedByLanguage() {
		assertEquals("en_us", LanguageModule.key(List.of("en_us")));
		assertEquals("en_us,de_de", LanguageModule.key(List.of("en_us", "de_de")));
		assertNotEquals(LanguageModule.key(List.of("en_us")), LanguageModule.key(List.of("en_us", "de_de")));
	}

	@Test
	void mapIsRebuiltFromTheStoredEntries() {
		ObjectObjectList<String, String> entries = new ObjectObjectList<>();
		entries.put("menu.singleplayer", "Singleplayer");
		entries.put("menu.multiplayer", "Multiplayer");

		Map<String, String> map = new LanguageModule.Translations(entries, false).asMap();

		assertEquals(2, map.size());
		assertEquals("Singleplayer", map.get("menu.singleplayer"));
		assertEquals("Multiplayer", map.get("menu.multiplayer"));
	}

	@Test
	void storedLanguageIsFoundByItsKey() {
		ObjectObjectList<String, String> entries = new ObjectObjectList<>();
		entries.put("a", "1");

		ObjectObjectList<String, LanguageModule.Translations> languages = new ObjectObjectList<>();
		languages.put("en_us,de_de", new LanguageModule.Translations(entries, true));

		LanguageModule.Translations found = LanguageModule.find(languages, List.of("en_us", "de_de"));
		assertTrue(found != null);
		assertTrue(found.rightToLeft);
		assertEquals("1", found.asMap().get("a"));

		assertNull(LanguageModule.find(languages, List.of("en_us", "fr_fr")));
		assertNull(LanguageModule.find(new ObjectObjectList<>(), List.of("en_us")));
		assertNull(LanguageModule.find(null, List.of("en_us")));
	}

	@Test
	void lookupsWithoutALoadedCacheReturnNull() {
		assertNull(LanguageModule.getLoad(List.of("en_us")));
	}
}