package dev.quantumfusion.dashloader.def.data.dataobject.mapping;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.common.IntObjectList;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.VanillaData;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;

@Data
public class DashBlockStateData implements Dashable<Object2IntMap<BlockState>> {
	public final IntObjectList<Integer> blockstates;

	public DashBlockStateData(IntObjectList<Integer> blockstates) {
		this.blockstates = blockstates;
	}

	public DashBlockStateData(VanillaData data, DashRegistryWriter writer, final DashLoader.TaskHandler taskHandler) {
		this.blockstates = new IntObjectList<>();
		final Object2IntMap<BlockState> stateLookup = data.getStateLookup();
		taskHandler.setSubtasks(stateLookup.size());
		stateLookup.forEach((blockState, integer) -> {
			this.blockstates.put(writer.add(blockState), integer);
			taskHandler.completedSubTask();
		});
	}

	public Object2IntMap<BlockState> export(DashRegistryReader reader) {
		final Object2IntOpenHashMap<BlockState> stateLookupOut = new Object2IntOpenHashMap<>();
		blockstates.forEach((key, value) -> stateLookupOut.put(reader.get(key), value));
		return stateLookupOut;
	}

}
