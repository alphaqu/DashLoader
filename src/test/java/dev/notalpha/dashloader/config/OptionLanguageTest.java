package dev.notalpha.dashloader.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionLanguageTest {
	private static final Pattern TRANSLATION = Pattern.compile("\"(config\\.[A-Za-z_.]+)\"\\s*:");
	private static final Pattern OPTION_KEY = Pattern.compile("^config\\.([A-Z_]+)(\\.tooltip)?$");

	private static Set<String> translations() throws IOException {
		try (InputStream in = OptionLanguageTest.class.getResourceAsStream("/dashloader/lang/en_us.json")) {
			assertTrue(in != null, "en_us.json not on the classpath");

			String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			Set<String> keys = new LinkedHashSet<>();
			Matcher matcher = TRANSLATION.matcher(json);
			while (matcher.find()) {
				keys.add(matcher.group(1));
			}

			return keys;
		}
	}

	@Test
	void everyOptionHasATranslation() throws IOException {
		Set<String> keys = translations();

		for (Option option : Option.values()) {
			String key = "config." + option.name();
			assertTrue(keys.contains(key), key + " has no label in en_us.json, the config screen would show the raw key");
			assertTrue(keys.contains(key + ".tooltip"), key + " has no tooltip in en_us.json");
		}
	}

	@Test
	void thereAreNoTranslationsWithoutAnOption() throws IOException {
		for (String key : translations()) {
			Matcher matcher = OPTION_KEY.matcher(key);
			if (!matcher.matches()) {
				continue;
			}

			assertTrue(hasOption(matcher.group(1)), key + " has no matching entry in Option");
		}
	}

	private static boolean hasOption(String name) {
		for (Option option : Option.values()) {
			if (option.name().equals(name)) {
				return true;
			}
		}

		return false;
	}
}