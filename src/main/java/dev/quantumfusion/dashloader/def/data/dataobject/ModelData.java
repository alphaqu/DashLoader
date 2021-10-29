package dev.quantumfusion.dashloader.def.data.dataobject;

import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.model.DashModel;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeSubclasses;
import net.minecraft.client.render.model.BakedModel;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.registry.RegistryStorageData;
import net.oskarstrom.dashloader.core.registry.RegistryStorageDataImpl;

public class ModelData implements RegistryDataObject {
	@Serialize(order = 0)
	@SerializeSubclasses(extraSubclassesId = "models", path = {0})
	public final RegistryStorageDataImpl<BakedModel, DashModel> modelData;

	public ModelData(@Deserialize("modelData") RegistryStorageDataImpl<BakedModel, DashModel> modelData) {
		this.modelData = modelData;
	}

	public ModelData(DashRegistry dashRegistry) {
		var storageMappings = DashLoader.getInstance().getApi().storageMappings;
		//noinspection unchecked
		this.modelData = (RegistryStorageDataImpl<BakedModel, DashModel>) dashRegistry.getStorage(storageMappings.getByte(DashDataType.MODEL));
	}

	@Override
	public void dumpData(DashExportHandler exportHandler) {
		exportHandler.addStorage(modelData);
	}

	@Override
	public int getSize() {
		return 1;
	}


}
