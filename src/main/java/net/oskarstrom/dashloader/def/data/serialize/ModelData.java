package net.oskarstrom.dashloader.def.data.serialize;

import io.activej.serializer.annotations.Serialize;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.registry.RegistryStorageData;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.api.DashDataType;
import net.oskarstrom.dashloader.def.model.DashModel;

public class ModelData implements RegistryDataObject {
	@Serialize(order = 0)
	public final RegistryStorageData<DashModel> modelData;

	public ModelData(RegistryStorageData<DashModel> modelData) {
		this.modelData = modelData;
	}

	public ModelData(DashRegistry dashRegistry) {
		var storageMappings = DashLoader.getInstance().getApi().storageMappings;
		//noinspection unchecked
		this.modelData = (RegistryStorageData<DashModel>) dashRegistry.getStorageData(storageMappings.getByte(DashDataType.MODEL));
	}

	@Override
	public void dumpData(DashRegistry dashRegistry) {
		dashRegistry.addStorage(modelData);
	}

	@Override
	public int getSize() {
		return 1;
	}


}
