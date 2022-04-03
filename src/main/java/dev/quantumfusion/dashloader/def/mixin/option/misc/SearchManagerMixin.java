package dev.quantumfusion.dashloader.def.mixin.option.misc;

import net.minecraft.client.search.SearchManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(SearchManager.class)
public abstract class SearchManagerMixin implements SynchronousResourceReloader {
	@Override
	public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
		return CompletableFuture.supplyAsync(() -> {
			// prepare
			this.reload(manager);
			return null;
		}, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenAcceptAsync((object) -> {}, applyExecutor);
	}
}
