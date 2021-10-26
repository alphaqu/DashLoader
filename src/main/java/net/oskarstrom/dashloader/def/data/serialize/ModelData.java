package net.oskarstrom.dashloader.def.data.serialize;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeSubclasses;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.NativeImage;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.registry.RegistryStorageData;
import net.oskarstrom.dashloader.core.registry.RegistryStorageDataImpl;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.api.DashDataType;
import net.oskarstrom.dashloader.def.image.DashImage;
import net.oskarstrom.dashloader.def.mixin.feature.cache.BakedModelManagerOverride;
import net.oskarstrom.dashloader.def.model.DashModel;

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
