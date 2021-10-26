package net.oskarstrom.dashloader.def.registry;

import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.registry.RegistryStorageFactory;

public class ModelRegistryStorage<F, D extends Dashable<F>> extends RegistryStorageFactory.SupplierRegistryImpl<F,D> {

	public ModelRegistryStorage(DashRegistry registry, D[] data) {
		super(registry, data);
	}

	@Override
	public F[] toUndash(DashExportHandler exportHandler) {
		return super.toUndash(registry);
	}
}
