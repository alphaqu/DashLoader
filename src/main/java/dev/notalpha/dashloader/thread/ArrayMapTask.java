package dev.notalpha.dashloader.thread;

import java.util.concurrent.RecursiveAction;
import java.util.function.Function;

public final class ArrayMapTask<I, O> extends RecursiveAction {
	private final int threshold;
	private final int start;
	private final int stop;
	private final I[] inArray;
	private final O[] outArray;
	private final Function<I, O> function;

	private ArrayMapTask(I[] inArray, O[] outArray, Function<I, O> function, int threshold, int start, int stop) {
		this.threshold = threshold;
		this.start = start;
		this.stop = stop;
		this.inArray = inArray;
		this.outArray = outArray;
		this.function = function;
	}

	public ArrayMapTask(I[] inArray, O[] outArray, Function<I, O> function) {
		this.start = 0;
		this.stop = inArray.length;
		this.threshold = ThreadHandler.calcThreshold(this.stop);
		this.inArray = inArray;
		this.outArray = outArray;
		this.function = function;
	}

	@Override
	protected void compute() {
		final int size = this.stop - this.start;
		if (size < this.threshold) {
			for (int i = this.start; i < this.stop; i++) {
				this.outArray[i] = this.function.apply(this.inArray[i]);
			}
		} else {
			final int middle = this.start + (size / 2);
			invokeAll(new ArrayMapTask<>(this.inArray, this.outArray, this.function, this.threshold, this.start, middle),
					new ArrayMapTask<>(this.inArray, this.outArray, this.function, this.threshold, middle, this.stop));
		}
	}
}
