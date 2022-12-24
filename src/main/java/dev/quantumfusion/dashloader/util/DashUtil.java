package dev.quantumfusion.dashloader.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class DashUtil {
	@Nullable
	public static <I, O> O nullable(@Nullable I in, @NotNull Function<I, O> func) {
		if (in == null) {
			return null;
		}
		return func.apply(in);
	}

	@Nullable
	public static <I1, I2, O> O nullable(@Nullable I1 in1, @Nullable I2 in2, @NotNull BiFunction<I1, I2, O> func) {
		if (in1 == null || in2 == null) {
			return null;
		}
		return func.apply(in1, in2);
	}

	public static <O> O[][] fragmentArray(O[] array, int fragments) {
		int size = array.length;
		O[][] out = (O[][]) new Object[fragments][];


		if (size <= fragments) {
			for (int i = 0; i < fragments; i++) {
				if (i < size) {
					out[i] = (O[]) new Object[]{array[i]};
				} else {
					out[i] = (O[]) new Object[]{};
				}
			}
		} else  {
			int averageSize = size / fragments;
			for (int i = 0; i < fragments; i++) {
				int fragStart = (averageSize * i);
				int fragEnd = i == fragments - 1 ? size : (averageSize * (i + 1));
				int fragSize = fragEnd - fragStart;

				O[] dashables = (O[]) new Object[fragSize];
				System.arraycopy(array, fragStart, dashables, 0, fragSize);
				out[i] = dashables;
			}
		}
		return out;
	}

	public static <O> O[] combineArray(O[][] fragments) {
		int size = 0;
		for (O[] fragment : fragments) {
			size += fragment.length;
		}

		O[] out = (O[]) new Object[size];
		int position = 0;
		for (O[] fragment : fragments) {
			System.arraycopy(fragment, 0, out, position, fragment.length);
			position += fragment.length;
		}

		return out;
	}
}
