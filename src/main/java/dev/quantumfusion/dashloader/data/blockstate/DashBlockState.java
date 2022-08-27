package dev.quantumfusion.dashloader.data.blockstate;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.api.DashDependencies;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.data.DashIdentifier;
import dev.quantumfusion.dashloader.data.DashModelIdentifier;
import dev.quantumfusion.dashloader.mixin.accessor.ModelLoaderAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@DashObject(BlockState.class)
@DashDependencies({DashIdentifier.class, DashModelIdentifier.class})
public final class DashBlockState implements Dashable<BlockState> {
	public static final Identifier ITEM_FRAME = new Identifier("dashloader:itemframewhy");
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

		if (owner == null) {
			throw new RuntimeException("Could not find a blockstate for " + blockState);
		}

		this.owner = writer.add(owner);
		this.pos = pos;
	}

	@Override
	public BlockState export(final RegistryReader reader) {
		final Identifier id = reader.get(this.owner);
		// if its item frame get its state from the model loader as mojank is mojank
		if (id.equals(ITEM_FRAME)) {
			return ModelLoaderAccessor.getTheItemFrameThing().getStates().get(this.pos);
		} else {
			return Registry.BLOCK.get(id).getStateManager().getStates().get(this.pos);
		}
	}
}
