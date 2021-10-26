package net.oskarstrom.dashloader.def.model.predicates;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeSubclasses;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.registry.Pointer;
import net.oskarstrom.dashloader.core.registry.RegistryStorageImpl;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.api.DashDataType;
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
	@SerializeSubclasses(extraSubclassesId = "predicates", path = {0})
	public final List<DashPredicate> selectors;

	public DashOrPredicate(@Deserialize("selectors") List<DashPredicate> selectors) {
		this.selectors = selectors;
	}

	public DashOrPredicate(OrMultipartModelSelector selector, DashRegistry registry) {
		OrMultipartModelSelectorAccessor access = ((OrMultipartModelSelectorAccessor) selector);
		selectors = new ArrayList<>();
		for (MultipartModelSelector accessSelector : access.getSelectors()) {
			selectors.add((DashPredicate) ((RegistryStorageImpl)registry.getStorage(DashLoader.getInstance().getApi().storageMappings.getByte(DashDataType.PREDICATE))).create(RegistryUtil.preparePredicate(accessSelector),registry));
		}
	}

	@Override
	public Predicate<BlockState> toUndash(DashExportHandler exportHandler) {
		Collection<Predicate<BlockState>> list = DashHelper.convertCollection(selectors, (s) -> s.toUndash(registry));
		return (blockState) -> list.stream().anyMatch((predicate) -> predicate.test(blockState));
	}
}
