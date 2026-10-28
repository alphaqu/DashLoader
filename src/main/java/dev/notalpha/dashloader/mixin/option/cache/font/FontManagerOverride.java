package dev.notalpha.dashloader.mixin.option.cache.font;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.font.FontModule;
import dev.notalpha.dashloader.mixin.accessor.FontManagerProviderIndexAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

@Mixin(FontManager.class)
public class FontManagerOverride {
	@Inject(
			method = "prepare",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void loadFonts(ResourceManager resourceManager, Executor executor, CallbackInfoReturnable<CompletableFuture<FontManager.Preparation>> cir) {
		FontModule.DATA.visit(CacheStatus.LOAD, data -> {
			// A cache without the default font (e.g. saved while every font was skipped)
			// would crash vanilla with "Default font failed to load" - fall back instead.
			if (data != null && data.providers.containsKey(Minecraft.DEFAULT_FONT)) {
				DashLoader.LOG.info("Providing fonts");
				cir.setReturnValue(CompletableFuture.completedFuture(FontManagerProviderIndexAccessor.create(data.providers, data.allProviders)));
			} else {
				DashLoader.LOG.warn("Font cache missing default font, falling back to vanilla loading");
			}
		});
	}

	@Inject(
			method = "apply(Lnet/minecraft/client/gui/font/FontManager$Preparation;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
			at = @At(value = "HEAD")
	)
	private void saveFonts(FontManager.Preparation index, ProfilerFiller profiler, CallbackInfo ci) {
		if (FontModule.DATA.active(CacheStatus.SAVE)) {
			DashLoader.LOG.info("Saving fonts");
			FontModule.DATA.set(CacheStatus.SAVE, new FontModule.ProviderIndex(index.fontSets(), index.allProviders()));
		}
	}
}
