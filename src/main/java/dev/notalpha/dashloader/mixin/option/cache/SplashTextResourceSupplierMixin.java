package dev.notalpha.dashloader.mixin.option.cache;

import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.splash.SplashModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.CommonColors;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * In 1.21.11 splash texts are {@code List<Text>} (yellow-styled literals), while the
 * DashLoader cache stores plain {@code List<String>}. Convert both directions so the
 * cached strings round-trip without a {@code ClassCastException} at runtime.
 */
@Mixin(SplashManager.class)
public class SplashTextResourceSupplierMixin {
	@Unique
	private static final Style DASHLOADER_SPLASH_STYLE = Style.EMPTY.withColor(CommonColors.YELLOW);

	@Inject(
			method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/List;",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void applySplashCache(ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfoReturnable<List<Component>> cir) {
		SplashModule.TEXTS.visit(CacheStatus.LOAD, strings ->
				cir.setReturnValue(strings.stream()
						.map(string -> Component.literal(string).setStyle(DASHLOADER_SPLASH_STYLE))
						.map(Component.class::cast)
						.toList()));
	}

	@Inject(
			method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/List;",
			at = @At(value = "RETURN")
	)
	private void stealSplashCache(ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfoReturnable<List<Component>> cir) {
		SplashModule.TEXTS.visit(CacheStatus.SAVE, strings -> {
			strings.clear();
			for (Component text : cir.getReturnValue()) {
				strings.add(text.getString());
			}
		});
	}
}
