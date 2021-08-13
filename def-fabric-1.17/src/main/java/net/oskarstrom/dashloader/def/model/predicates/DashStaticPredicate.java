package net.oskarstrom.dashloader.def.model.predicates;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.oskarstrom.dashloader.api.registry.DashRegistry;

import java.util.function.Predicate;

public class DashStaticPredicate implements DashPredicate {

	@Serialize(order = 0)
	public final boolean value;

	public DashStaticPredicate(@Deserialize("value") boolean value) {
		this.value = value;
	}


	public DashStaticPredicate(MultipartModelSelector multipartModelSelector) {
		value = multipartModelSelector == MultipartModelSelector.TRUE;
	}

	@Override
	public Predicate<BlockState> toUndash(DashRegistry registry) {
		return (blockState) -> value;
	}

}
