package dev.notalpha.dashloader.util.mixins;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.minecraft.model.ModelCacheHandler;
import dev.notalpha.dashloader.mixin.accessor.AndMultipartModelSelectorAccessor;
import dev.notalpha.dashloader.mixin.accessor.OrMultipartModelSelectorAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.render.model.json.AndMultipartModelSelector;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;
import net.minecraft.state.StateManager;

public class MixinThings {
	public static FontManager FONTMANAGER;

	public static <M extends MultipartModelSelector> void addPredicates(Iterable<M> multipartModelSelectors, StateManager<Block, BlockState> stateStateManager) {
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

		ModelCacheHandler.STATE_MANAGERS.visit(DashLoader.Status.SAVE, map -> {
			map.put(multipartModelSelector, stateStateManager);
		});
	}

}
