package net.oskarstrom.dashloader.def.blockstate;

import com.google.common.collect.ImmutableMap;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.oskarstrom.dashloader.api.Dashable;
import net.oskarstrom.dashloader.api.data.Pointer2PointerMap;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.registry.Pointer;
import net.oskarstrom.dashloader.def.mixin.accessor.StateAccessor;


public class DashBlockState implements Dashable<BlockState> {

	@Serialize(order = 0)
	public final Pointer owner;

	@Serialize(order = 1)
	public final Pointer2PointerMap entriesEncoded;


	public DashBlockState(@Deserialize("owner") Pointer owner,
						  @Deserialize("entriesEncoded") Pointer2PointerMap entriesEncoded) {
		this.owner = owner;
		this.entriesEncoded = entriesEncoded;
	}

	public DashBlockState(BlockState blockState, DashRegistry registry) {
		StateAccessor<Block, BlockState> accessState = ((StateAccessor<Block, BlockState>) blockState);
		entriesEncoded = new Pointer2PointerMap();
		accessState.getEntries().forEach((property, comparable) -> {
			entriesEncoded.add(Pointer2PointerMap.Entry.of(registry.add(property), registry.add(comparable)));
		});
		owner = registry.add(Registry.BLOCK.getId(blockState.getBlock()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public final BlockState toUndash(final DashRegistry registry) {
		final ImmutableMap.Builder<Property<?>, Comparable<?>> builder = ImmutableMap.builder();
		entriesEncoded.forEach((entry) -> builder.put(registry.get(entry.key), registry.get(entry.value)));
		return new BlockState(Registry.BLOCK.get((Identifier) registry.get(owner)), builder.build(), null);
	}

}
