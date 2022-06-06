package dev.quantumfusion.dashloader.mixin.option.cache;

import dev.quantumfusion.dashloader.DashDataManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static dev.quantumfusion.dashloader.DashLoader.DL;

@Mixin(SplashTextResourceSupplier.class)
public class SplashTextResourceSupplierMixin {

	@Shadow
	@Final
	private static Identifier RESOURCE_ID;

	@Inject(
			method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Ljava/util/List;",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void applySplashCache(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<List<String>> cir) {
		try {

			final DashDataManager.DashDataHandler<List<String>> splashText = DL.getData().splashText;
			if (DL.isRead() && splashText.dataAvailable()) {
				cir.setReturnValue(splashText.getCacheResultData());
			} else {
				Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(RESOURCE_ID);
				List<String> splashTexts;
				try {
					try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
						splashTexts = bufferedReader.lines().map(String::trim).filter((string) -> string.hashCode() != 125780783).collect(Collectors.toList());
					}
				} finally {
					if (resource != null) {
						resource.close();
					}

				}
				splashText.setMinecraftData(splashTexts);
				cir.setReturnValue(splashTexts);
			}
		} catch (IOException var36) {
			cir.setReturnValue(Collections.emptyList());
		}
	}
}
