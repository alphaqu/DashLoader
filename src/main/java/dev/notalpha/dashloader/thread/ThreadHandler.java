package dev.notalpha.dashloader.thread;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.registry.data.ChunkData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

public final class ThreadHandler {
	public static final int THREADS = Runtime.getRuntime().availableProcessors();
	public static final ThreadHandler INSTANCE = new ThreadHandler();
	private final ForkJoinPool threadPool = new ForkJoinPool(THREADS, new ForkJoinPool.ForkJoinWorkerThreadFactory() {
		private final AtomicInteger threadNumber = new AtomicInteger(0);

		@Override
		public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
			final ForkJoinWorkerThread dashThread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
			dashThread.setDaemon(true);
			dashThread.setName("dlc-thread-" + this.threadNumber.getAndIncrement());
			return dashThread;
		}
	}, null, false);

	private ThreadHandler() {
	}

	public static int calcThreshold(final int tasks) {
		return Math.max(tasks / (THREADS * 8), 4);
	}

	// Fork Join Methods
	public <R, D extends DashObject<? extends R, ?>> void parallelExport(ChunkData.Entry<D>[] in, R[] out, RegistryReader reader) {
		this.threadPool.invoke(new IndexedArrayMapTask<>(in, out, d -> d.export(reader)));
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

	/**
	 * Runs every callable and calls {@code onEach} on the calling thread once per
	 * callable, in the order the futures complete.
	 * <p>
	 * Reporting progress from inside the callables themselves is unsafe: the
	 * counters behind {@link dev.notalpha.taski.builtin.StepTask} are plain ints,
	 * so concurrent {@code next()} calls lose updates and the task never reaches
	 * 100%. Doing it here keeps the updates on a single thread.
	 */
	public <O> void forEachCompleted(Collection<Callable<O>> callables, Runnable onEach) {
		final ExecutorCompletionService<O> service = new ExecutorCompletionService<>(this.threadPool);
		final List<Future<O>> futures = new ArrayList<>(callables.size());
		for (Callable<O> callable : callables) {
			futures.add(service.submit(callable));
		}

		try {
			for (int i = 0; i < futures.size(); i++) {
				this.acquire(service.take());
				onEach.run();
			}
		} catch (InterruptedException e) {
			futures.forEach(future -> future.cancel(true));
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} catch (RuntimeException e) {
			// fail fast: a broken fragment invalidates the whole cache anyway
			futures.forEach(future -> future.cancel(true));
			throw e;
		}
	}

	private <O> O acquire(Future<O> future) {
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}

