package dev.quantumfusion.dashloader.def.data.dataobject.mapping;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.font.Font;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.data.Pointer2ObjectMap;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.VanillaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashFontManagerData implements Dashable<Map<Identifier, List<Font>>> {

	@Serialize(order = 0)
	public final Pointer2ObjectMap<List<Integer>> fontMap;

	public DashFontManagerData(@Deserialize("fontMap") Pointer2ObjectMap<List<Integer>> fontMap) {
		this.fontMap = fontMap;
	}

	public DashFontManagerData(VanillaData data, DashRegistry registry, DashLoader.TaskHandler taskHandler) {
		fontMap = new Pointer2ObjectMap<>();
		int amount = 0;
		final Map<Identifier, List<Font>> fonts = data.getFonts();
		for (List<Font> value : fonts.values()) {
			amount += value.size();
		}
		taskHandler.setSubtasks(amount);
		fonts.forEach((identifier, fontList) -> {
			List<Integer> fontsOut = new ArrayList<>();
			fontList.forEach(font -> {
				fontsOut.add(registry.add(font));
				taskHandler.completedSubTask();
			});
			fontMap.add(Pointer2ObjectMap.Entry.of(registry.add(identifier), fontsOut));
		});
	}

	public Map<Identifier, List<Font>> toUndash(DashExportHandler exportHandler) {
		Map<Identifier, List<Font>> out = new HashMap<>();
		fontMap.forEach((entry) -> {
			List<Font> fontsOut = new ArrayList<>();
			entry.value.forEach(fontPointer -> fontsOut.add(exportHandler.get(fontPointer)));
			out.put(exportHandler.get(entry.key), fontsOut);
		});
		return out;
	}
}
