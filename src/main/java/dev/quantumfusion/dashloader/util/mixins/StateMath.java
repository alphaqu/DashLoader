package dev.quantumfusion.dashloader.util.mixins;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class StateMath {

	// comparable to value.
	public static <S> Object[][] createWithFast(
			Comparable<?>[] values,
			Comparable<?>[][] propertyValues,
			Object2ObjectMap<Comparable<?>[], S> states,
			int size
	) {
		Object[][] out = new Object[values.length][];

		// Value getter to save reinit
		Comparable<?>[] valueGetter = new Comparable[size];
		System.arraycopy(values, 0, valueGetter, 0, size);

		for (int propPos = 0; propPos < size; propPos++) {
			var value = values[propPos];
			var propValues = propertyValues[propPos];

			final int length = propValues.length;
			Object[] outRow = new Object[length];

			for (int i = 0; i < length; i++) {
				valueGetter[propPos] = propValues[i];
				outRow[i] = states.get(valueGetter);
			}

			out[propPos] = outRow;
			valueGetter[propPos] = value;
		}

		return out;
	}

	public static List<Pair<ImmutableMap<Property<?>, Comparable<?>>, Comparable<?>[]>> generateCombinations2Mod(Property<?>[] properties, Comparable<?>[][] propertyValues) {
		final int length = properties.length;
		int size = 1;
		for (Property<?> list : properties) {
			size *= list.getValues().size();
		}
		var out = new ArrayList<Pair<ImmutableMap<Property<?>, Comparable<?>>, Comparable<?>[]>>(size);
		switch (length) {
			case 1 -> {
				final var p1 = properties[0];
				final var v1 = propertyValues[0];
				for (var o1 : v1) {
					out.add(Pair.of(ImmutableMap.of(p1, o1), new Comparable[]{o1}));
				}
			}
			case 2 -> {
				final var p1 = properties[0];
				final var p2 = properties[1];
				final var v1 = propertyValues[0];
				final var v2 = propertyValues[1];
				for (var o1 : v1) {
					for (var o2 : v2) {
						out.add(Pair.of(ImmutableMap.of(p1, o1, p2, o2), new Comparable[]{o1, o2}));
					}
				}
			}
			case 3 -> {
				final var p1 = properties[0];
				final var p2 = properties[1];
				final var p3 = properties[2];
				final var v1 = propertyValues[0];
				final var v2 = propertyValues[1];
				final var v3 = propertyValues[2];
				for (var o1 : v1) {
					for (var o2 : v2) {
						for (var o3 : v3) {
							out.add(Pair.of(ImmutableMap.of(p1, o1, p2, o2, p3, o3), new Comparable[]{o1, o2, o3}));
						}
					}
				}
			}
			case 4 -> {
				final var p1 = properties[0];
				final var p2 = properties[1];
				final var p3 = properties[2];
				final var p4 = properties[3];
				final var v1 = propertyValues[0];
				final var v2 = propertyValues[1];
				final var v3 = propertyValues[2];
				final var v4 = propertyValues[3];
				for (var o1 : v1) {
					for (var o2 : v2) {
						for (var o3 : v3) {
							for (var o4 : v4) {
								out.add(Pair.of(ImmutableMap.of(p1, o1, p2, o2, p3, o3, p4, o4), new Comparable[]{o1, o2, o3, o4}));
							}
						}
					}
				}
			}
			case 5 -> {
				final var p1 = properties[0];
				final var p2 = properties[1];
				final var p3 = properties[2];
				final var p4 = properties[3];
				final var p5 = properties[4];
				final var v1 = propertyValues[0];
				final var v2 = propertyValues[1];
				final var v3 = propertyValues[2];
				final var v4 = propertyValues[3];
				final var v5 = propertyValues[4];
				for (var o1 : v1) {
					for (var o2 : v2) {
						for (var o3 : v3) {
							for (var o4 : v4) {
								for (var o5 : v5) {
									out.add(Pair.of(ImmutableMap.of(p1, o1, p2, o2, p3, o3, p4, o4, p5, o5), new Comparable[]{o1, o2, o3, o4, o5}));
								}
							}
						}
					}
				}
			}
			default -> {
				for (Comparable<?>[] comparables : generateCombinations2Mod(properties, 0, size, propertyValues)) {
					ImmutableMap.Builder<Property<?>, Comparable<?>> builder = ImmutableMap.builder();
					for (int i = 0; i < comparables.length; i++) {
						builder.put(properties[i], comparables[i]);
					}
					out.add(Pair.of(builder.build(), comparables));
				}
			}
		}
		return out;
	}

	public static List<Comparable<?>[]> generateCombinations2Mod(Property<?>[] properties, int pos, int size, Comparable<?>[][] propertyValues) {
		List<Comparable<?>[]> resultLists = new ArrayList<>(size);
		final int length = properties.length;
		if (length == pos) {
			resultLists.add(new Comparable[length]);
			return resultLists;
		} else {
			List<Comparable<?>[]> remainingLists = generateCombinations2Mod(properties, pos + 1, size, propertyValues);
			for (Comparable<?> condition : propertyValues[pos]) {
				for (Comparable<?>[] remainingList : remainingLists) {
					Comparable<?>[] resultList = new Comparable[length];
					System.arraycopy(remainingList, 0, resultList, 0, length);
					resultList[pos] = condition;
					resultLists.add(resultList);
				}
			}
		}
		return resultLists;
	}

	public static <S extends State<?, S>, T extends Comparable<T>> MapCodec<S> createCodec(MapCodec<S> mapCodec, Supplier<S> defaultStateGetter, String key, Property<T> property) {
		return Codec.mapPair(mapCodec, property.getValueCodec().fieldOf(key).orElseGet(string -> {
		}, () -> property.createValue(defaultStateGetter.get()))).xmap(pair -> pair.getFirst().with(property, (pair.getSecond()).value()), state -> Pair.of(state, property.createValue(state)));

	}

	public static Comparable<?>[][] createPropertyValues(Property<?>[] properties) {
		final int length = properties.length;
		Comparable<?>[][] out = new Comparable[length][];

		for (int i = 0; i < length; i++) {
			out[i] = properties[i].getValues().toArray(Comparable[]::new);
		}

		return out;
	}
}
