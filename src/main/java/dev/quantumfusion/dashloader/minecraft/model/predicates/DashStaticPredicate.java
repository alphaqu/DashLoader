package dev.quantumfusion.dashloader.minecraft.model.predicates;

import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;

import java.util.function.Predicate;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashStaticPredicate that = (DashStaticPredicate) o;

		return value == that.value;
	}

	@Override
	public int hashCode() {
		return (value ? 1 : 0);
	}
}
