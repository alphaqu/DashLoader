package dev.quantumfusion.dashloader.def.data.model.predicates;

import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.def.util.BooleanSelector;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;

import java.util.function.Predicate;

@Data
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
	public Predicate<BlockState> export(RegistryReader exportHandler) {
		return value ? MultipartModelSelector.TRUE.getPredicate(null) : MultipartModelSelector.FALSE.getPredicate(null);
	}

}
