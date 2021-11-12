package dev.quantumfusion.dashloader.def.corehook.holder;

import dev.quantumfusion.dashloader.core.DashLoaderCore;
import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.common.IntIntList;
import dev.quantumfusion.dashloader.core.progress.task.CountTask;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.core.registry.chunk.write.AbstractWriteChunk;
import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.data.blockstate.DashBlockState;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class DashBlockStateData implements Dashable<Object2IntMap<BlockState>> {
	public final IntIntList blockstates;

	public DashBlockStateData(IntIntList blockstates) {
		this.blockstates = blockstates;
	}

	public DashBlockStateData(DashDataManager data, RegistryWriter writer) {
		this.blockstates = new IntIntList();
		final Object2IntMap<BlockState> stateLookup = data.modelStateLookup.getMinecraftData();
		final AbstractWriteChunk<BlockState, DashBlockState> chunk = writer.getChunk(DashBlockState.class);

		CountTask task = new CountTask(stateLookup.size());
		DashLoaderCore.PROGRESS.getCurrentContext().setSubtask(task);
		stateLookup.object2IntEntrySet().forEach((e) -> {
			this.blockstates.put(writer.addDirect(chunk, e.getKey()), e.getIntValue());
			task.completedTask();
		});
	}

	public Object2IntMap<BlockState> export(RegistryReader reader) {
		var states = new Object2IntOpenHashMap<BlockState>();
		blockstates.forEach((key, value) -> states.put(reader.get(key), value));
		return states;
	}

}
