package dev.notalpha.dashloader.mixin.option.cache.font;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.font.FontModule;
import dev.notalpha.dashloader.mixin.accessor.FontManagerPreparationAccessor;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(FontManager.class)
public class FontManagerOverride {
	@Inject(
			method = "prepare",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void loadFonts(ResourceManager resourceManager, Executor executor, CallbackInfoReturnable<CompletableFuture<?>> cir) {
		FontModule.DATA.visit(CacheStatus.LOAD, data -> {
			DashLoader.LOG.info("Providing fonts");
			cir.setReturnValue(CompletableFuture.completedFuture(FontManagerPreparationAccessor.create(data.providers, data.allProviders)));
		});
	}

	@Inject(
			method = "apply",
			at = @At(value = "HEAD")
	)
	private void saveFonts(Object preparation, Object profiler, CallbackInfo ci) {
		if (FontModule.DATA.active(CacheStatus.SAVE)) {
			DashLoader.LOG.info("Saving fonts");
			FontManagerPreparationAccessor access = (FontManagerPreparationAccessor) preparation;
			FontModule.DATA.set(CacheStatus.SAVE, new FontModule.ProviderIndex(access.getFontSets(), access.getAllProviders()));
		}
	}
}
