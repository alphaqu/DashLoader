package dev.notalpha.dashloader.thread;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.registry.RegistryReaderImpl;
import dev.notalpha.dashloader.registry.data.ChunkData;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
	}, null, true);

	private ThreadHandler() {
	}

	public static int calcThreshold(final int tasks) {
		return Math.max(tasks / (THREADS * 8), 4);
	}

	public <R, D extends DashObject<? extends R>> void parallelExport(ChunkData.Entry<D>[] in, R[] out, RegistryReaderImpl reader) {
		this.threadPool.invoke(new IndexedArrayMapTask<>(in, out, d -> d.export(reader)));
	}
	public void parallelRunnable(Collection<Runnable> runnables) {
		for (Future<Object> future : this.threadPool.invokeAll(runnables.stream().map(Executors::callable).toList())) {
			this.acquire(future);
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
