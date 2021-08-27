package net.oskarstrom.dashloader.def.model.predicates;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeSubclasses;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.registry.Pointer;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.mixin.accessor.OrMultipartModelSelectorAccessor;
import net.oskarstrom.dashloader.def.util.RegistryUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;


@DashObject(OrMultipartModelSelector.class)
public class DashOrPredicate implements DashPredicate {
	@Serialize(order = 0)
	public final List<Integer> selectors;

	public DashOrPredicate(@Deserialize("selectors") List<Integer> selectors) {
		this.selectors = selectors;
	}

	public DashOrPredicate(OrMultipartModelSelector selector, DashRegistry registry) {
		OrMultipartModelSelectorAccessor access = ((OrMultipartModelSelectorAccessor) selector);
		selectors = new ArrayList<>();
		for (MultipartModelSelector accessSelector : access.getSelectors()) {
			selectors.add(registry.add(RegistryUtil.preparePredicate(accessSelector)));
		}
	}

	@Override
	public Predicate<BlockState> toUndash(DashRegistry registry) {
		Collection<Predicate<BlockState>> list = DashHelper.convertCollection(selectors, registry::get);
		return (blockState) -> list.stream().anyMatch((predicate) -> predicate.test(blockState));
	}
}
