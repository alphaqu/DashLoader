package dev.quantumfusion.dashloader.def.mixin.option.cache;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.mixin.accessor.StateManagerAccessor;
import dev.quantumfusion.dashloader.def.util.UnsafeHelper;
import dev.quantumfusion.dashloader.def.util.mixins.StateDuck;
import dev.quantumfusion.dashloader.def.util.mixins.StateMath;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(StateManager.Builder.class)
public abstract class StateManagerBuilderMixin<O, S extends State<O, S>> {

	@Shadow
	@Final
	private O owner;

	@Shadow
	@Final
	private Map<String, Property<?>> namedProperties;

	@Shadow
	public abstract StateManager.Builder<O, S> add(Property<?>... properties);

	/**
	 * @author notequalalpha
	 */
	@Overwrite
	public StateManager<O, S> build(Function<O, S> ownerToStateFunction, StateManager.Factory<O, S> factory) {
		ImmutableSortedMap<String, Property<?>> properties = ImmutableSortedMap.copyOf(namedProperties);
		Supplier<S> supplier = () -> ownerToStateFunction.apply(owner);
		MapCodec<S> mapCodec = MapCodec.of(Encoder.empty(), Decoder.unit(supplier));

		for (var entry : properties.entrySet()) {
			mapCodec = StateMath.createCodec(mapCodec, supplier, entry.getKey(), entry.getValue());
		}

		final Property[] propertiesRaw = namedProperties.values().toArray(Property[]::new);
		final Comparable<?>[][] propertyValues = StateMath.createPropertyValues(propertiesRaw);

		MapCodec<S> finalMapCodec = mapCodec;

		List<S> stateList = new ArrayList<>();
		List<Comparable<?>[]> stateValues = new ArrayList<>();
		Object2ObjectMap<Comparable<?>[], S> stateLookup = new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
			@Override
			public int hashCode(Comparable<?>[] o) {
				return Arrays.hashCode(o);
			}

			@Override
			public boolean equals(Comparable<?>[] a, Comparable<?>[] b) {
				return Arrays.equals(a, b);
			}
		});

		// mine
		for (var entry : StateMath.generateCombinations2Mod(propertiesRaw, propertyValues)) {
			final ImmutableMap<Property<?>, Comparable<?>> entries = entry.getFirst();
			S state = factory.create(owner, entries, finalMapCodec);
			stateLookup.put(entry.getSecond(), state);
			stateValues.add(entry.getSecond());
			stateList.add(state);
		}

		for (int i = 0; i < stateValues.size(); i++) {
			final StateDuck<O, S> access = (StateDuck<O, S>) stateList.get(i);
			final Comparable<?>[] values = stateValues.get(i);
			access.setFastWithTable(StateMath.createWithFast(values, propertyValues, stateLookup, propertiesRaw.length));
			access.setPropertiesMap(propertiesRaw);
			access.setValuesMap(propertyValues);
		}

		final StateManager<O, S> stateManager = UnsafeHelper.allocateInstance(StateManager.class);
		final StateManagerAccessor<O, S> access = (StateManagerAccessor<O, S>) stateManager;
		access.setOwner(owner);
		access.setStates(ImmutableList.copyOf(stateList));
		access.setProperties(properties);

		return stateManager;
	}


}
