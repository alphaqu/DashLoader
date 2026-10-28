package dev.notalpha.dashloader.client.blockstate;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DashBlockState implements DashObject<BlockState, BlockState> {
	// The index of a state inside its block's state list, built once per block.
	// The linear scan this replaces ran once per cached blockstate, so a save
	// cost O(sum of N^2) BlockState.equals over all possible states.
	private static final Map<Block, Map<BlockState, Integer>> STATE_INDEX = new ConcurrentHashMap<>();

	public final int owner;
	public final int pos;

	public DashBlockState(int owner, int pos) {
		this.owner = owner;
		this.pos = pos;
	}

	public DashBlockState(BlockState blockState, RegistryWriter writer) {
		Block block = blockState.getBlock();
		Map<BlockState, Integer> index = STATE_INDEX.computeIfAbsent(block, DashBlockState::indexOf);
		Integer found = index.get(blockState);
		Identifier owner = Registries.BLOCK.getId(block);

		if (owner == null || found == null) {
			throw new RuntimeException("Could not find a blockstate for " + blockState);
		}

		this.owner = writer.add(owner);
		this.pos = found;
	}

	private static Map<BlockState, Integer> indexOf(Block block) {
		var states = block.getStateManager().getStates();
		Map<BlockState, Integer> index = new HashMap<>(states.size() * 2);
		for (int i = 0; i < states.size(); i++) {
			index.putIfAbsent(states.get(i), i);
		}
		return index;
	}

	@Override
	public BlockState export(final RegistryReader reader) {
		final Identifier id = reader.get(this.owner);
		return Registries.BLOCK.get(id).getStateManager().getStates().get(this.pos);
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

