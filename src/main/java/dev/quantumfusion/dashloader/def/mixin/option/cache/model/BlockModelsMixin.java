package dev.quantumfusion.dashloader.def.mixin.option.cache.model;

import dev.quantumfusion.dashloader.def.DashLoader;
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
			method = "getModelId(Lnet/minecraft/util/Identifier;Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/util/ModelIdentifier;",
			at = @At(value = "RETURN")
	)
	private static void snagModelId(Identifier id, BlockState state, CallbackInfoReturnable<ModelIdentifier> cir) {
		if (DashLoader.isWrite()) {
			DashLoader.getData().getWriteContextData().blockStates.put(cir.getReturnValue(), state);
		}
	}
}
