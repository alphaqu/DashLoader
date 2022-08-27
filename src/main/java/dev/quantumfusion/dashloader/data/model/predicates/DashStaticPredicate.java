package dev.quantumfusion.dashloader.data.model.predicates;

import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.util.BooleanSelector;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;

@DashObject(BooleanSelector.class)
public final class DashStaticPredicate implements DashPredicate {
	public final boolean value;

	public DashStaticPredicate(boolean value) {
		this.value = value;
	}

	public DashStaticPredicate(BooleanSelector multipartModelSelector) {
		this.value = multipartModelSelector.selector;
	}

	@Override
	public Predicate<BlockState> export(RegistryReader exportHandler) {
		return this.value ? MultipartModelSelector.TRUE.getPredicate(null) : MultipartModelSelector.FALSE.getPredicate(null);
	}

}
