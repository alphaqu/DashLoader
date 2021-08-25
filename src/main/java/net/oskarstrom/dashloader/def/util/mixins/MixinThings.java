package net.oskarstrom.dashloader.def.util.mixins;

import it.unimi.dsi.fastutil.ints.IntComparator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.render.model.json.AndMultipartModelSelector;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;
import net.minecraft.state.StateManager;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.mixin.accessor.AndMultipartModelSelectorAccessor;
import net.oskarstrom.dashloader.def.mixin.accessor.OrMultipartModelSelectorAccessor;

import java.util.function.BiFunction;

public class MixinThings {
	public static FontManager fontManager;


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
		DashLoader.getVanillaData().addPredicateStateManager(multipartModelSelector, stateStateManager);
	}

}
