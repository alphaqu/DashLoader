package dev.quantumfusion.dashloader.def.corehook.holder;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.common.IntIntList;
import dev.quantumfusion.dashloader.core.common.IntObjectList;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.core.registry.chunk.write.AbstractChunkWriter;
import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.data.blockstate.DashBlockState;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;

@Data
public class DashBlockStateData implements Dashable<Object2IntMap<BlockState>> {
	public final IntIntList blockstates;

	public DashBlockStateData(IntIntList blockstates) {
		this.blockstates = blockstates;
	}

	public DashBlockStateData(DashDataManager data, DashRegistryWriter writer) {
		this.blockstates = new IntIntList();
		final Object2IntMap<BlockState> stateLookup = data.modelStateLookup.getMinecraftData();
		final AbstractChunkWriter<BlockState, DashBlockState> chunk = writer.getChunk(DashBlockState.class);
		stateLookup.object2IntEntrySet().forEach((e) -> this.blockstates.put(writer.addDirect(chunk, e.getKey()), e.getIntValue()));
	}

	public Object2IntMap<BlockState> export(DashRegistryReader reader) {
		var states = new Object2IntOpenHashMap<BlockState>();
		blockstates.forEach((key, value) -> states.put(reader.get(key), value));
		return states;
	}

}
