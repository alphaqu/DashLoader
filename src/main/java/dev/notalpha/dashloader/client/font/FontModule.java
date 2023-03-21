package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.*;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.cache.DashCache;
import dev.notalpha.dashloader.api.cache.DashModule;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.config.Option;
import dev.notalpha.dashloader.api.collection.IntObjectList;
import dev.notalpha.dashloader.api.cache.CachingData;
import dev.quantumfusion.taski.builtin.StepTask;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.stb.STBTTFontinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FontModule implements DashModule<FontModule.Data> {
	public static FontManager FONTMANAGER;
	public static final CachingData<Object2ObjectMap<Identifier, Pair<Int2ObjectMap<IntList>, List<Font>>>> DATA = new CachingData<>();
	public static final CachingData<Map<STBTTFontinfo, Identifier>> FONT_TO_IDENT = new CachingData<>();

	@Override
	public void reset(DashCache cacheManager) {
		DATA.reset(cacheManager, new Object2ObjectOpenHashMap<>());
		FONT_TO_IDENT.reset(cacheManager, new HashMap<>());
	}

	@Override
	public Data save(RegistryWriter writer, StepTask task) {
		var fontMap = new IntObjectList<DashFontStorage>();
		Object2ObjectMap<Identifier, Pair<Int2ObjectMap<IntList>, List<Font>>> identifierPairObject2ObjectMap = DATA.get(CacheStatus.SAVE);
		identifierPairObject2ObjectMap.forEach((identifier, fontList) -> {
			List<Integer> fontsOut = new ArrayList<>();
			for (Font font : fontList.getValue()) {
				fontsOut.add(writer.add(font));
			}
			IntObjectList<List<Integer>> charactersByWidth = new IntObjectList<>();
			fontList.getKey().forEach(charactersByWidth::put);
			fontMap.put(writer.add(identifier), new DashFontStorage(charactersByWidth, fontsOut));
			task.next();
		});

		return new Data(fontMap);
	}

	@Override
	public void load(Data mappings, RegistryReader reader, StepTask task) {
		Object2ObjectMap<Identifier, Pair<Int2ObjectMap<IntList>, List<Font>>> out = new Object2ObjectOpenHashMap<>();
		mappings.fontMap.forEach((key, value) -> {
			List<Font> fontsOut = new ArrayList<>();
			value.fonts.forEach(fontPointer -> fontsOut.add(reader.get(fontPointer)));

			Int2ObjectMap<IntList> charactersByWidth = new Int2ObjectOpenHashMap<>();
			value.charactersByWidth.forEach((key1, value1) -> charactersByWidth.put(key1, new IntArrayList(value1)));
			out.put(reader.get(key), Pair.of(charactersByWidth, fontsOut));
		});

		DATA.set(CacheStatus.LOAD, out);
	}

	@Override
	public Class<Data> getDataClass() {
		return Data.class;
	}

	@Override
	public boolean isActive() {
		return ConfigHandler.optionActive(Option.CACHE_FONT);
	}

	public static final class Data {
		public final IntObjectList<DashFontStorage> fontMap;

		public Data(IntObjectList<DashFontStorage> fontMap) {
			this.fontMap = fontMap;
		}
	}

	public static final class DashFontStorage {
		public final IntObjectList<List<Integer>> charactersByWidth;
		public final List<Integer> fonts;

		public DashFontStorage(IntObjectList<List<Integer>> charactersByWidth, List<Integer> fonts) {
			this.charactersByWidth = charactersByWidth;
			this.fonts = fonts;
		}
	}
}
