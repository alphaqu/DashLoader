package dev.notalpha.dashloader.mixin.option.cache.font;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.font.FontModule;
import dev.notalpha.dashloader.mixin.accessor.FontManagerProviderIndexAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
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
			method = "loadIndex",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void loadFonts(ResourceManager resourceManager, Executor executor, CallbackInfoReturnable<CompletableFuture<FontManager.ProviderIndex>> cir) {
		FontModule.DATA.visit(CacheStatus.LOAD, data -> {
			// A cache without the default font (e.g. saved while every font was skipped)
			// breaks vanilla font loading - fall back instead.
			if (data != null && data.providers.containsKey(MinecraftClient.DEFAULT_FONT_ID)) {
				DashLoader.LOG.info("Providing fonts");
				cir.setReturnValue(CompletableFuture.completedFuture(FontManagerProviderIndexAccessor.create(data.providers, data.allProviders)));
			} else {
				DashLoader.LOG.warn("Font cache missing default font, falling back to vanilla loading");
			}
		});
	}

	@Inject(
			method = "reload(Lnet/minecraft/client/font/FontManager$ProviderIndex;Lnet/minecraft/util/profiler/Profiler;)V",
			at = @At(value = "HEAD")
	)
	private void saveFonts(FontManager.ProviderIndex index, Profiler profiler, CallbackInfo ci) {
		if (FontModule.DATA.active(CacheStatus.SAVE)) {
			DashLoader.LOG.info("Saving fonts");
			FontModule.DATA.set(CacheStatus.SAVE, new FontModule.ProviderIndex(index.fontSets(), index.allProviders()));
		}
	}
}
