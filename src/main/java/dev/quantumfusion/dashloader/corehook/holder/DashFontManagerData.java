package dev.quantumfusion.dashloader.corehook.holder;

import dev.quantumfusion.dashloader.DashDataManager;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.common.IntObjectList;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.taski.builtin.StepTask;
import net.minecraft.client.font.Font;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashFontManagerData implements Dashable<Map<Identifier, List<Font>>> {
	public final IntObjectList<List<Integer>> fontMap;

	public DashFontManagerData(IntObjectList<List<Integer>> fontMap) {
		this.fontMap = fontMap;
	}

	public DashFontManagerData(DashDataManager data, RegistryWriter writer, StepTask parent) {
		this.fontMap = new IntObjectList<>();
		parent.run(new StepTask("Fonts", data.fonts.getMinecraftData().size()), (task) -> {
			data.fonts.getMinecraftData().forEach((identifier, fontList) -> {
				List<Integer> fontsOut = new ArrayList<>();
				fontList.forEach(font -> fontsOut.add(writer.add(font)));
				this.fontMap.put(writer.add(identifier), fontsOut);
				task.next();
			});
		});
	}

	public Map<Identifier, List<Font>> export(RegistryReader reader) {
		Map<Identifier, List<Font>> out = new HashMap<>();
		this.fontMap.forEach((key, value) -> {
			List<Font> fontsOut = new ArrayList<>();
			value.forEach(fontPointer -> fontsOut.add(reader.get(fontPointer)));
			out.put(reader.get(key), fontsOut);
		});
		return out;
	}
}
