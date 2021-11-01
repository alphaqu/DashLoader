package dev.quantumfusion.dashloader.def.mixin.feature.cache;

import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.mixin.accessor.MultipartModelComponentAccessor;
import dev.quantumfusion.dashloader.def.util.mixins.MixinThings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(MultipartUnbakedModel.class)
public class MultipartUnbakedModelMixin {


	@Shadow
	@Final
	private List<MultipartModelComponent> components;

	@Shadow
	@Final
	private StateManager<Block, BlockState> stateFactory;

	@Inject(
			method = "bake(Lnet/minecraft/client/render/model/ModelLoader;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/model/BakedModel;",
			at = @At(value = "RETURN"),
			locals = LocalCapture.CAPTURE_FAILSOFT,
			cancellable = true
	)
	private void addPredicateInfo(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId, CallbackInfoReturnable<BakedModel> cir, MultipartBakedModel.Builder builder) {
		if (DashLoader.isWrite()) {
			var bakedModel = (MultipartBakedModel) builder.build();
			var outSelectors = new ArrayList<MultipartModelSelector>();

			components.forEach(multipartModelComponent -> outSelectors.add(((MultipartModelComponentAccessor) multipartModelComponent).getSelector()));
			DashLoader.getData().getWriteContextData().multipartPredicates.put(bakedModel, Pair.of(outSelectors, stateFactory));
			MixinThings.addPredicates(outSelectors, stateFactory);
			cir.setReturnValue(bakedModel);
		}
	}


}
