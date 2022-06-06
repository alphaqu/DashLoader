package dev.quantumfusion.dashloader.util.mixins;

import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.mixin.accessor.AndMultipartModelSelectorAccessor;
import dev.quantumfusion.dashloader.mixin.accessor.OrMultipartModelSelectorAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.render.model.json.AndMultipartModelSelector;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;
import net.minecraft.state.StateManager;

public class MixinThings {
	public static FontManager FONTMANAGER;
	public static long BOOTSTRAP_START;
	public static long BOOTSTRAP_END;

	public static long FALLBACK_MODELS_COUNT = -1;
	public static long CACHED_MODELS_COUNT = -1;


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
		DashLoader.getData().getWriteContextData().stateManagers.put(multipartModelSelector, stateStateManager);
	}

}
