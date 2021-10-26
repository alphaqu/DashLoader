package net.oskarstrom.dashloader.def.model.predicates;

import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.oskarstrom.dashloader.core.registry.Pointer;
import net.oskarstrom.dashloader.core.registry.RegistryStorageImpl;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.api.DashDataType;
import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.mixin.accessor.AndMultipartModelSelectorAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeSubclasses;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.AndMultipartModelSelector;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.util.RegistryUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@DashObject(AndMultipartModelSelector.class)
public class DashAndPredicate implements DashPredicate {
	@Serialize(order = 0)
	@SerializeSubclasses(extraSubclassesId = "predicates", path = {0})
	public final List<DashPredicate> selectors;

	public DashAndPredicate(@Deserialize("selectors") List<DashPredicate> selectors) {
		this.selectors = selectors;
	}

	public DashAndPredicate(AndMultipartModelSelector selector, DashRegistry registry) {
		AndMultipartModelSelectorAccessor access = ((AndMultipartModelSelectorAccessor) selector);
		selectors = new ArrayList<>();
		for (MultipartModelSelector accessSelector : access.getSelectors()) {
			selectors.add((DashPredicate) ((RegistryStorageImpl)registry.getStorage(DashLoader.getInstance().getApi().storageMappings.getByte(DashDataType.PREDICATE))).create(RegistryUtil.preparePredicate(accessSelector),registry));
		}
	}

	@Override
	public Predicate<BlockState> toUndash(DashExportHandler exportHandler) {
		Collection<Predicate<BlockState>> list = DashHelper.convertCollection(selectors, (s) -> s.toUndash(registry));
		return (blockState) -> list.stream().allMatch((predicate) -> predicate.test(blockState));
	}
}
