package dev.quantumfusion.dashloader.thread;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.factory.DashFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

public final class ThreadHandler {
	public static final int CORES = Runtime.getRuntime().availableProcessors();

	private final ForkJoinPool threadPool = new ForkJoinPool(CORES, new ForkJoinPool.ForkJoinWorkerThreadFactory() {
		private final AtomicInteger threadNumber = new AtomicInteger(0);

		@Override
		public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
			final ForkJoinWorkerThread dashThread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
			dashThread.setDaemon(true);
			dashThread.setName("dlc-thread-" + this.threadNumber.getAndIncrement());
			return dashThread;
		}
	}, null, true);

	public ThreadHandler() {
	}

	public static int calcThreshold(final int tasks) {
		return Math.max(tasks / (CORES * 32), 4);
	}

	// Fork Join Methods
	public <R, D extends Dashable<? extends R>> void parallelExport(dev.quantumfusion.dashloader.thread.IndexedArrayMapTask.IndexedArrayEntry<D>[] in, R[] out, RegistryReader reader) {
		this.threadPool.invoke(new IndexedArrayMapTask<>(in, out, d -> d.export(reader)));
	}

	public <R, D extends Dashable<? extends R>> void parallelExport(D[] in, R[] out, RegistryReader reader) {
		this.threadPool.invoke(new dev.quantumfusion.dashloader.thread.ArrayMapTask<>(in, out, d -> d.export(reader)));
	}

	@SuppressWarnings("unchecked")
	public <R, D extends Dashable<? extends R>> void parallelWrite(R[] in, D[] out, RegistryWriter writer, DashFactory<R, ? extends D> factory) {
		this.threadPool.invoke(new ArrayMapTask<>(in, out, d -> factory.create(d, writer)));
	}

	// Basic Methods
	public void parallelRunnable(Runnable... runnables) {
		this.parallelRunnable(List.of(runnables));
	}

	public void parallelRunnable(Collection<Runnable> runnables) {
		for (Future<Object> future : this.threadPool.invokeAll(runnables.stream().map(Executors::callable).toList())) {
			this.acquire(future);
		}
	}

	@SafeVarargs
	public final <O> O[] parallelCallable(IntFunction<O[]> creator, Callable<O>... callables) {
		O[] out = creator.apply(callables.length);
		var futures = this.threadPool.invokeAll(List.of(callables));
		for (int i = 0, futuresSize = futures.size(); i < futuresSize; i++) {
			out[i] = (this.acquire(futures.get(i)));
		}
		return out;
	}

	public <O> Collection<O> parallelCallable(Collection<Callable<O>> callables) {
		List<O> out = new ArrayList<>();
		var futures = this.threadPool.invokeAll(callables);
		for (Future<O> future : futures) {
			out.add(this.acquire(future));
		}
		return out;
	}

	private <O> O acquire(Future<O> future) {
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}


}
