package dev.quantumfusion.dashloader.data.mapping;

import dev.quantumfusion.dashloader.DashDataManager;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.data.common.IntObjectList;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.taski.builtin.StepTask;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.font.Font;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashFontManagerData implements Dashable<Map<Identifier, Pair<Int2ObjectMap<IntList>, List<Font>>>> {
	public final IntObjectList<DashFontStorage> fontMap;

	public DashFontManagerData(IntObjectList<DashFontStorage> fontMap) {
		this.fontMap = fontMap;
	}

	public DashFontManagerData(DashDataManager data, RegistryWriter writer, StepTask parent) {
		this.fontMap = new IntObjectList<>();
		parent.run(new StepTask("Fonts", Integer.max(data.fonts.getMinecraftData().size(), 1)), (task) -> {
			data.fonts.getMinecraftData().forEach((identifier, fontList) -> {
				List<Integer> fontsOut = new ArrayList<>();
				for (Font font : fontList.getValue()) {
					fontsOut.add(writer.add(font));
				}
				IntObjectList<List<Integer>> charactersByWidth = new IntObjectList<>();
				fontList.getKey().forEach(charactersByWidth::put);
				this.fontMap.put(writer.add(identifier), new DashFontStorage(charactersByWidth, fontsOut));
				task.next();
			});
		});
	}

	public Map<Identifier, Pair<Int2ObjectMap<IntList>, List<Font>>> export(RegistryReader reader) {
		Map<Identifier, Pair<Int2ObjectMap<IntList>, List<Font>>> out = new HashMap<>();
		this.fontMap.forEach((key, value) -> {
			List<Font> fontsOut = new ArrayList<>();
			value.fonts.forEach(fontPointer -> fontsOut.add(reader.get(fontPointer)));

			Int2ObjectMap<IntList> charactersByWidth = new Int2ObjectOpenHashMap<>();
			value.charactersByWidth.forEach((key1, value1) -> charactersByWidth.put(key1, new IntArrayList(value1)));
			out.put(reader.get(key), Pair.of(charactersByWidth, fontsOut));
		});
		return out;
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
