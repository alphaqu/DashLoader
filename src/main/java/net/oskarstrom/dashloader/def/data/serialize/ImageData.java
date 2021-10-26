package net.oskarstrom.dashloader.def.data.serialize;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.texture.NativeImage;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.registry.RegistryStorage;
import net.oskarstrom.dashloader.core.registry.RegistryStorageDataImpl;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.api.DashDataType;
import net.oskarstrom.dashloader.def.image.DashImage;

public class ImageData implements RegistryDataObject {
	@Serialize(order = 0)
	public final RegistryStorageDataImpl<NativeImage, DashImage> imageData;

	public ImageData(@Deserialize("imageData") RegistryStorageDataImpl<NativeImage, DashImage> imageData) {
		this.imageData = imageData;
	}

	public ImageData(DashRegistry dashRegistry) {
		var storageMappings = DashLoader.getInstance().getApi().storageMappings;
		final byte pos = storageMappings.getByte(DashDataType.NATIVEIMAGE);
		final RegistryStorage<?> storage = dashRegistry.getStorage(pos);
		this.imageData = new RegistryStorageDataImpl<>((DashImage[]) storage.getDashables(), pos, (short) 0);
	}

	@Override
	public void dumpData(DashExportHandler dashRegistry) {
		dashRegistry.addStorage(imageData, imageData.registryPos);
	}

	@Override
	public int getSize() {
		return 1;
	}
}
