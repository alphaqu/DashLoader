package dev.quantumfusion.dashloader.mixin.accessor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StateManager.class)
public interface StateManagerAccessor<O, S extends State<O, S>> {
	@Accessor
	@Mutable
	void setOwner(O owner);

	@Accessor
	@Mutable
	void setProperties(ImmutableSortedMap<String, Property<?>> properties);

	@Accessor
	@Mutable
	void setStates(ImmutableList<S> states);


}
