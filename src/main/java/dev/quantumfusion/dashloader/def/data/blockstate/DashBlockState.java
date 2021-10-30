package dev.quantumfusion.dashloader.def.data.blockstate;

import com.google.common.collect.ImmutableMap;
import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.api.annotation.DashDependencies;
import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.common.IntIntList;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.data.DashIdentifier;
import dev.quantumfusion.dashloader.def.data.DashModelIdentifier;
import dev.quantumfusion.dashloader.def.data.blockstate.property.DashBooleanProperty;
import dev.quantumfusion.dashloader.def.data.blockstate.property.value.DashBooleanValue;
import dev.quantumfusion.dashloader.def.data.blockstate.property.value.DashPropertyValueManager;
import dev.quantumfusion.dashloader.def.mixin.accessor.StateAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;

@Data
@DashObject(BlockState.class)
@DashDependencies({DashBooleanValue.class, DashBooleanProperty.class, DashIdentifier.class, DashModelIdentifier.class})
public record DashBlockState(int owner, IntIntList entriesEncoded) implements Dashable<BlockState> {

	public DashBlockState(BlockState blockState, DashRegistryWriter writer) {
		this(writer.add(Registry.BLOCK.getId(blockState.getBlock())), new IntIntList(new ArrayList<>()));

		StateAccessor<Block, BlockState> accessState = ((StateAccessor<Block, BlockState>) blockState);
		accessState.getEntries().forEach((property, comparable) -> {
			this.entriesEncoded.put(writer.add(property), writer.add(DashPropertyValueManager.prepare(comparable)));
		});
	}

	@Override
	public BlockState export(final DashRegistryReader reader) {
		final ImmutableMap.Builder<Property<?>, Comparable<?>> builder = ImmutableMap.builder();
		entriesEncoded.forEach((key, value) -> builder.put(reader.get(key), reader.get(value)));
		return new BlockState(Registry.BLOCK.get((Identifier) reader.get(owner)), builder.build(), null);
	}

}
