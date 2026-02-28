package dev.notalpha.dashloader.client.model.predicates;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;

import java.util.function.Predicate;

public class BooleanSelector implements Condition {
public final boolean selector;

public BooleanSelector(boolean selector) {
this.selector = selector;
}

	public BooleanSelector(Condition selector) {
		this.selector = selector instanceof BooleanSelector b && b.selector;
	}

	@Override
	public <O, S extends StateHolder<O, S>> Predicate<S> instantiate(StateDefinition<O, S> stateFactory) {
		return stateHolder -> selector;
	}
}
