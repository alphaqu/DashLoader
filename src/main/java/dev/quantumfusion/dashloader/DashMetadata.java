package dev.quantumfusion.dashloader;

import dev.quantumfusion.dashloader.api.DashCacheHandler;
import dev.quantumfusion.dashloader.api.DashObjectClass;
import dev.quantumfusion.dashloader.api.Dashable;

import java.util.ArrayList;
import java.util.List;

public class DashMetadata {
	private final List<DashObjectClass<?, ?>> dashObjects;
	private final List<DashCacheHandler<?>> cacheHandlers;

	public DashMetadata(List<DashObjectClass<?, ?>> dashObjects, List<DashCacheHandler<?>> cacheHandlers) {
		this.dashObjects = dashObjects;
		this.cacheHandlers = cacheHandlers;
	}

	public static final class Factory {
		private final List<DashObjectClass<?, ?>> dashObjects = new ArrayList<>();
		private final List<DashCacheHandler<?>> cacheHandlers = new ArrayList<>();
		private boolean failed = false;

		@SafeVarargs
		public final void registerDashObjects(Class<? extends Dashable<?>>... classes) {
			for (Class<? extends Dashable<?>> aClass : classes) {
				registerDashObject(aClass);
			}
		}

		public void registerDashObject(Class<? extends Dashable<?>> aClass) {
			final Class<?>[] interfaces = aClass.getInterfaces();
			if (interfaces.length == 0) {
				DashLoader.LOG.error("No Interfaces found. Class: {}", aClass.getSimpleName());
				this.failed = true;
				return;
			}
			this.dashObjects.add(new DashObjectClass<>(aClass));
		}

		public void registerCacheHandler(DashCacheHandler<?> handler) {
			if (handler.isActive()) {
				this.cacheHandlers.add(handler);
			} else {
				DashLoader.LOG.warn("Cache handler " + handler.getClass() + " is added while being inactive.");
			}
		}

		public DashMetadata build() {
			if (failed) {
				throw new RuntimeException("Failed to initialize the API");
			}
			return new DashMetadata(this.dashObjects, this.cacheHandlers);
		}
	}
}
