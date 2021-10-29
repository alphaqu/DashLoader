package dev.quantumfusion.dashloader.def;

import dev.quantumfusion.dashloader.def.data.blockstate.property.DashProperty;
import dev.quantumfusion.dashloader.def.data.blockstate.property.value.DashPropertyValue;
import dev.quantumfusion.dashloader.def.data.font.DashFont;
import dev.quantumfusion.dashloader.def.data.model.DashModel;
import dev.quantumfusion.dashloader.def.data.model.predicates.DashPredicate;
import net.oskarstrom.dashloader.core.serializer.DashSerializer;
import net.oskarstrom.dashloader.core.serializer.DashSerializerManager;
import dev.quantumfusion.dashloader.def.data.dataobject.ImageData;
import dev.quantumfusion.dashloader.def.data.dataobject.MappingData;
import dev.quantumfusion.dashloader.def.data.dataobject.ModelData;
import dev.quantumfusion.dashloader.def.data.dataobject.RegistryData;

import java.nio.file.Path;

public class DashSerializers {
	public static DashSerializer<ModelData> MODEL_SERIALIZER;
	public static DashSerializer<RegistryData> REGISTRY_SERIALIZER;
	public static DashSerializer<MappingData> MAPPING_SERIALIZER;
	public static DashSerializer<ImageData> IMAGE_SERIALIZER;

	public static void initSerializers() {
		final DashLoader instance = DashLoader.getInstance();
		final DashSerializerManager serializerManager = instance.getSerializerManager();
		final Path resourcePackBoundDir = instance.getResourcePackBoundDir();

		MODEL_SERIALIZER = serializerManager.loadOrCreateSerializer("model", ModelData.class, resourcePackBoundDir, DashModel.class);
		REGISTRY_SERIALIZER = serializerManager.loadOrCreateSerializer("registry", RegistryData.class, resourcePackBoundDir, DashPredicate.class, DashFont.class, DashProperty.class, DashPropertyValue.class);
		MAPPING_SERIALIZER = serializerManager.loadOrCreateSerializer("mapping", MappingData.class, resourcePackBoundDir);
		IMAGE_SERIALIZER = serializerManager.loadOrCreateSerializer("image", ImageData.class, resourcePackBoundDir);
	}
}
