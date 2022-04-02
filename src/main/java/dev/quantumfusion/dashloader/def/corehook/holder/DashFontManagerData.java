package dev.quantumfusion.dashloader.def.corehook.holder;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.common.IntObjectList;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.taski.builtin.StepTask;
import net.minecraft.client.font.Font;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DashFontManagerData implements Dashable<Map<Identifier, List<Font>>> {
	public final IntObjectList<List<Integer>> fontMap;

	public DashFontManagerData(IntObjectList<List<Integer>> fontMap) {
		this.fontMap = fontMap;
	}

	public DashFontManagerData(DashDataManager data, RegistryWriter writer, StepTask parent) {
		fontMap = new IntObjectList<>();
		parent.run(new StepTask("Fonts", data.fonts.getMinecraftData().size()), (task) -> {
			data.fonts.getMinecraftData().forEach((identifier, fontList) -> {
				List<Integer> fontsOut = new ArrayList<>();
				fontList.forEach(font -> fontsOut.add(writer.add(font)));
				fontMap.put(writer.add(identifier), fontsOut);
				task.next();
			});
		});
	}

	public Map<Identifier, List<Font>> export(RegistryReader reader) {
		Map<Identifier, List<Font>> out = new HashMap<>();
		fontMap.forEach((key, value) -> {
			List<Font> fontsOut = new ArrayList<>();
			value.forEach(fontPointer -> fontsOut.add(reader.get(fontPointer)));
			out.put(reader.get(key), fontsOut);
		});
		return out;
	}
}
