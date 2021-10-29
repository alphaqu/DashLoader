package dev.quantumfusion.dashloader.def.data.blockstate;

import com.google.common.collect.ImmutableMap;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.data.IntIntList;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import dev.quantumfusion.dashloader.def.mixin.accessor.StateAccessor;

import java.util.ArrayList;

@Data
public record DashBlockState(int owner, IntIntList entriesEncoded) implements Dashable<BlockState> {

	public DashBlockState(BlockState blockState, DashRegistry registry) {
		this(registry.add(Registry.BLOCK.getId(blockState.getBlock())), new IntIntList(new ArrayList<>()));

		StateAccessor<Block, BlockState> accessState = ((StateAccessor<Block, BlockState>) blockState);
		accessState.getEntries().forEach((property, comparable) ->
				this.entriesEncoded.put(registry.add(property), registry.add(comparable)));
	}

	@SuppressWarnings("unchecked")
	@Override
	public BlockState toUndash(final DashExportHandler registry) {
		final ImmutableMap.Builder<Property<?>, Comparable<?>> builder = ImmutableMap.builder();
		entriesEncoded.forEach((key, value) -> builder.put(registry.get(key), registry.get(value)));
		return new BlockState(Registry.BLOCK.get((Identifier) registry.get(owner)), builder.build(), null);
	}

}
