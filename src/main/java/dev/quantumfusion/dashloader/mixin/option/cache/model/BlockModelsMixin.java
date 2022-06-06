package dev.quantumfusion.dashloader.mixin.option.cache.model;

import dev.quantumfusion.dashloader.DashLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModels.class)
public class BlockModelsMixin {

	@Inject(
			method = "getModelId(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/util/ModelIdentifier;",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private static void cacheModelId(BlockState state, CallbackInfoReturnable<ModelIdentifier> cir) {
		if (DashLoader.isRead()) {
			final Identifier identifier = DashLoader.getData().getReadContextData().missingModelsRead.get(state);
			if (identifier != null) {
				cir.setReturnValue((ModelIdentifier) identifier);
			}
		}
	}
}
