package dev.quantumfusion.dashloader.def.data.blockstate;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.api.DashDependencies;
import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.data.DashIdentifier;
import dev.quantumfusion.dashloader.def.data.DashModelIdentifier;
import dev.quantumfusion.dashloader.def.mixin.accessor.ModelLoaderAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Data
@DashObject(BlockState.class)
@DashDependencies({DashIdentifier.class, DashModelIdentifier.class})
public final class DashBlockState implements Dashable<BlockState> {
	public transient static final Identifier ITEM_FRAME = new Identifier("dashloader:itemframewhy");
	public final int owner;
	public final int pos;

	public DashBlockState(int owner, int pos) {
		this.owner = owner;
		this.pos = pos;
	}

	public DashBlockState(BlockState blockState, RegistryWriter writer) {
		var block = blockState.getBlock();
		int pos = -1;

		Identifier owner = null;
		{
			var states = ModelLoaderAccessor.getTheItemFrameThing().getStates();
			for (int i = 0; i < states.size(); i++) {
				BlockState state = states.get(i);
				if (state.equals(blockState)) {
					pos = i;
					owner = ITEM_FRAME;
					break;
				}
			}
		}

		if (pos == -1) {
			var states = block.getStateManager().getStates();
			for (int i = 0; i < states.size(); i++) {
				BlockState state = states.get(i);
				if (state.equals(blockState)) {
					pos = i;
					owner = Registry.BLOCK.getId(block);
					break;
				}
			}
		}

		if (owner == null)
			throw new RuntimeException("Could not find a blockstate for " + blockState);

		this.owner = writer.add(owner);
		this.pos = pos;
	}

	@Override
	public BlockState export(final RegistryReader reader) {
		final Identifier id = reader.get(owner);
		// if its itemframe get its state from the modelloader as mojank is mojank
		if (id.equals(ITEM_FRAME)) {
			return ModelLoaderAccessor.getTheItemFrameThing().getStates().get(pos);
		} else {
			return Registry.BLOCK.get(id).getStateManager().getStates().get(pos);
		}
	}
}
