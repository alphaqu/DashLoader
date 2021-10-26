package net.oskarstrom.dashloader.def.model.predicates;

import net.minecraft.block.BlockState;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.util.BooleanSelector;

import java.util.function.Predicate;

@DashObject(BooleanSelector.class)
public class DashStaticPredicate implements DashPredicate {
	public final boolean value;

	public DashStaticPredicate(boolean value) {
		this.value = value;
	}


	public DashStaticPredicate(BooleanSelector multipartModelSelector) {
		value = multipartModelSelector.selector;
	}

	@Override
	public Predicate<BlockState> toUndash(DashExportHandler exportHandler) {
		return (blockState) -> value;
	}

}
