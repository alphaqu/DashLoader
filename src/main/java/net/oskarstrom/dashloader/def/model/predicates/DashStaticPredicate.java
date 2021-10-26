package net.oskarstrom.dashloader.def.model.predicates;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.SimpleMultipartModelSelector;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.util.BooleanSelector;

import java.util.function.Predicate;

@DashObject(BooleanSelector.class)
public class DashStaticPredicate implements DashPredicate {

	@Serialize(order = 0)
	public final boolean value;

	public DashStaticPredicate(@Deserialize("value") boolean value) {
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
