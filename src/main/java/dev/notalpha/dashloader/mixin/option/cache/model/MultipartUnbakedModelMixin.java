package dev.notalpha.dashloader.mixin.option.cache.model;

import dev.notalpha.dashloader.cache.CacheManager;
import dev.notalpha.dashloader.minecraft.model.ModelCacheHandler;
import dev.notalpha.dashloader.mixin.accessor.AndMultipartModelSelectorAccessor;
import dev.notalpha.dashloader.mixin.accessor.MultipartModelComponentAccessor;
import dev.notalpha.dashloader.mixin.accessor.OrMultipartModelSelectorAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.AndMultipartModelSelector;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
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
			method = "bake",
			at = @At(value = "RETURN"),
			locals = LocalCapture.CAPTURE_FAILSOFT,
			cancellable = true
	)
	private void addPredicateInfo(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId, CallbackInfoReturnable<@Nullable BakedModel> cir, MultipartBakedModel.Builder builder) {
		ModelCacheHandler.MULTIPART_PREDICATES.visit(CacheManager.Status.SAVE, map -> {
			var bakedModel = (MultipartBakedModel) builder.build();
			var outSelectors = new ArrayList<MultipartModelSelector>();
			this.components.forEach(multipartModelComponent -> outSelectors.add(((MultipartModelComponentAccessor) multipartModelComponent).getSelector()));
			map.put(bakedModel, Pair.of(outSelectors, this.stateFactory));
			addPredicates(outSelectors, this.stateFactory);
			cir.setReturnValue(bakedModel);
		});
	}

	private static <M extends MultipartModelSelector> void addPredicates(Iterable<M> multipartModelSelectors, StateManager<Block, BlockState> stateStateManager) {
		for (M multipartModelSelector : multipartModelSelectors) {
			addPredicate(multipartModelSelector, stateStateManager);
		}
	}

	private static void addPredicate(MultipartModelSelector multipartModelSelector, StateManager<Block, BlockState> stateStateManager) {
		if (multipartModelSelector instanceof AndMultipartModelSelector and) {
			addPredicates(((AndMultipartModelSelectorAccessor) and).getSelectors(), stateStateManager);
		} else if (multipartModelSelector instanceof OrMultipartModelSelector or) {
			addPredicates(((OrMultipartModelSelectorAccessor) or).getSelectors(), stateStateManager);
		}

		ModelCacheHandler.STATE_MANAGERS.visit(CacheManager.Status.SAVE, map -> {
			map.put(multipartModelSelector, stateStateManager);
		});
	}

}
