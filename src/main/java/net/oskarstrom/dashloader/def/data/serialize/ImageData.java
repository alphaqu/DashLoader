package net.oskarstrom.dashloader.def.data.serialize;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.registry.RegistryStorageData;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.api.DashDataType;
import net.oskarstrom.dashloader.def.api.DashLoaderAPI;
import net.oskarstrom.dashloader.def.image.DashImage;

public class ImageData implements RegistryDataObject{
	@Serialize(order = 0)
	public final RegistryStorageData<DashImage> imageData;

	public ImageData(@Deserialize("imageData")  RegistryStorageData<DashImage> imageData) {
		this.imageData = imageData;
	}

	public ImageData(DashRegistry dashRegistry) {
		var storageMappings = DashLoader.getInstance().getApi().storageMappings;
		//noinspection unchecked
		this.imageData = (RegistryStorageData<DashImage>) dashRegistry.getStorageData(storageMappings.getByte(DashDataType.NATIVEIMAGE));
	}

	@Override
	public void dumpData(DashRegistry dashRegistry) {
		dashRegistry.addStorage(imageData);
	}

	@Override
	public int getSize() {
		return 1;
	}
}
