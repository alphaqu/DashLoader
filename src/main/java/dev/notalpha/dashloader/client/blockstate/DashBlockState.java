package dev.notalpha.dashloader.client.blockstate;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.mixin.accessor.ModelLoaderAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class DashBlockState implements DashObject<BlockState, BlockState> {
	public static final ResourceLocation ITEM_FRAME = ResourceLocation.fromNamespaceAndPath("dashloader", "itemframewhy");
	public final int owner;
	public final int pos;

	public DashBlockState(int owner, int pos) {
		this.owner = owner;
		this.pos = pos;
	}

	public DashBlockState(BlockState blockState, RegistryWriter writer) {
		var block = blockState.getBlock();
		int pos = -1;

		ResourceLocation owner = null;
		{
			var states = ModelLoaderAccessor.getTheItemFrameThing().getPossibleStates();
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
			var states = block.getStateDefinition().getPossibleStates();
			for (int i = 0; i < states.size(); i++) {
				BlockState state = states.get(i);
				if (state.equals(blockState)) {
					pos = i;
					owner = BuiltInRegistries.BLOCK.getKey(block);
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
		final ResourceLocation id = reader.get(this.owner);
		// if its item frame get its state from the model loader as mojank is mojank
		if (id.equals(ITEM_FRAME)) {
			return ModelLoaderAccessor.getTheItemFrameThing().getPossibleStates().get(this.pos);
		} else {
			return BuiltInRegistries.BLOCK.getValue(id).getStateDefinition().getPossibleStates().get(this.pos);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashBlockState that = (DashBlockState) o;

		if (owner != that.owner) return false;
		return pos == that.pos;
	}

	@Override
	public int hashCode() {
		int result = owner;
		result = 31 * result + pos;
		return result;
	}
}
