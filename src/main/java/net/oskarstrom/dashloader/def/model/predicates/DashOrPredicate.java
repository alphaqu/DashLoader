package net.oskarstrom.dashloader.def.model.predicates;

import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.mixin.accessor.OrMultipartModelSelectorAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeSubclasses;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;
import net.minecraft.state.StateManager;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.ExtraVariables;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.mixin.accessor.OrMultipartModelSelectorAccessor;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@DashObject(OrMultipartModelSelector.class)
public class DashOrPredicate implements DashPredicate {
	@Serialize(order = 0)
	@SerializeSubclasses(path = {0}, extraSubclassesId = "predicates")
	public final List<DashPredicate> selectors;

	public DashOrPredicate(@Deserialize("selectors") List<DashPredicate> selectors) {
		this.selectors = selectors;
	}

	public DashOrPredicate(OrMultipartModelSelector selector, DashRegistry registry, ExtraVariables extraVariables) {
		//TODO statemanager
		StateManager<Block, BlockState> stateManager = (StateManager<Block, BlockState>) extraVariables.getExtraVariable1();
		OrMultipartModelSelectorAccessor access = ((OrMultipartModelSelectorAccessor) selector);
		selectors = DashHelper.convertCollection(access.getSelectors(), selector1 -> registry.predicates.obtainPredicate(selector1, stateManager));
	}

	@Override
	public Predicate<BlockState> toUndash(DashRegistry registry) {
		List<Predicate<BlockState>> list = selectors.stream().map(dashPredicate -> dashPredicate.toUndash(registry)).collect(Collectors.toList());
		return (blockState) -> list.stream().anyMatch((predicate) -> predicate.test(blockState));
	}
}
