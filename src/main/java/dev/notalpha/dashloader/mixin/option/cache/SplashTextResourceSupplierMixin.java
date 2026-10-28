package dev.notalpha.dashloader.mixin.option.cache;

import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.splash.SplashModule;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * In 1.21.11 splash texts are {@code List<Text>} (yellow-styled literals), while the
 * DashLoader cache stores plain {@code List<String>}. Convert both directions so the
 * cached strings round-trip without a {@code ClassCastException} at runtime.
 */
@Mixin(SplashTextResourceSupplier.class)
public class SplashTextResourceSupplierMixin {
	@Unique
	private static final Style DASHLOADER_SPLASH_STYLE = Style.EMPTY.withColor(Colors.YELLOW);

	@Inject(
			method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Ljava/util/List;",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void applySplashCache(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<List<Text>> cir) {
		SplashModule.TEXTS.visit(CacheStatus.LOAD, strings ->
				cir.setReturnValue(strings.stream()
						.map(string -> Text.literal(string).setStyle(DASHLOADER_SPLASH_STYLE))
						.map(Text.class::cast)
						.toList()));
	}

	@Inject(
			method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Ljava/util/List;",
			at = @At(value = "RETURN")
	)
	private void stealSplashCache(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<List<Text>> cir) {
		SplashModule.TEXTS.visit(CacheStatus.SAVE, strings -> {
			strings.clear();
			for (Text text : cir.getReturnValue()) {
				strings.add(text.getString());
			}
		});
	}
}
