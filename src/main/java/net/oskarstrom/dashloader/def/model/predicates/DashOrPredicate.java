package net.oskarstrom.dashloader.def.model.predicates;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.registry.storage.RegistryStorageImpl;
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


@Data
@DashObject(OrMultipartModelSelector.class)
public class DashOrPredicate implements DashPredicate {
	public final List<DashPredicate> selectors;

	public DashOrPredicate(List<DashPredicate> selectors) {
		this.selectors = selectors;
	}

	public DashOrPredicate(OrMultipartModelSelector selector, DashRegistry registry) {
		OrMultipartModelSelectorAccessor access = ((OrMultipartModelSelectorAccessor) selector);
		selectors = new ArrayList<>();
		for (MultipartModelSelector accessSelector : access.getSelectors()) {
			selectors.add((DashPredicate) ((RegistryStorageImpl) registry.getStorage(DashLoader.getInstance().getApi().storageMappings.getByte(DashDataType.PREDICATE))).create(RegistryUtil.preparePredicate(accessSelector), registry));
		}
	}

	@Override
	public Predicate<BlockState> toUndash(DashExportHandler handler) {
		Collection<Predicate<BlockState>> list = DashHelper.convertCollection(selectors, (s) -> s.toUndash(handler));
		return (blockState) -> list.stream().anyMatch((predicate) -> predicate.test(blockState));
	}
}
