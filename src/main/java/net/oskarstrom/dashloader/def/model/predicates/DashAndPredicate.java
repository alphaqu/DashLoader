package net.oskarstrom.dashloader.def.model.predicates;

import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.mixin.accessor.AndMultipartModelSelectorAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeSubclasses;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.AndMultipartModelSelector;
import net.minecraft.state.StateManager;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@DashObject(AndMultipartModelSelector.class)
public class DashAndPredicate implements DashPredicate {
	@Serialize(order = 0)
	@SerializeSubclasses(path = {0}, extraSubclassesId = "predicates")
	public final List<DashPredicate> selectors;

	public DashAndPredicate(@Deserialize("selectors") List<DashPredicate> selectors) {
		this.selectors = selectors;
	}

	public DashAndPredicate(AndMultipartModelSelector selector, DashRegistry registry) {
		//TODO statemanager
		StateManager<Block, BlockState> stateManager = (StateManager<Block, BlockState>) extraVariables.getExtraVariable1();
		AndMultipartModelSelectorAccessor access = ((AndMultipartModelSelectorAccessor) selector);
		selectors = DashHelper.convertCollection(access.getSelectors(), selector1 -> registry.predicates.obtainPredicate(selector1, stateManager));
	}

	@Override
	public Predicate<BlockState> toUndash(DashRegistry registry) {
		Collection<Predicate<BlockState>> list = DashHelper.convertCollection(selectors, predicate -> predicate.toUndash(registry));
		return (blockState) -> list.stream().allMatch((predicate) -> predicate.test(blockState));
	}
}
