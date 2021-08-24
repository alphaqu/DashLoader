package net.oskarstrom.dashloader.def.data;

import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.data.serialize.ImageData;
import net.oskarstrom.dashloader.def.data.serialize.MappingData;
import net.oskarstrom.dashloader.def.data.serialize.ModelData;
import net.oskarstrom.dashloader.def.data.serialize.RegistryData;
import net.oskarstrom.dashloader.def.util.enums.DashCachePaths;
import net.oskarstrom.dashloader.api.serializer.DashSerializer;
import net.oskarstrom.dashloader.api.serializer.DashSerializerManager;

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

		serializerManager.loadOrCreateSerializer("model",    ModelData.class,    resourcePackBoundDir, "models");
		serializerManager.loadOrCreateSerializer("registry", RegistryData.class, resourcePackBoundDir, "predicates", "fonts", "properties", "values");
		serializerManager.loadOrCreateSerializer("mapping",  MappingData.class,  resourcePackBoundDir);
		serializerManager.loadOrCreateSerializer("image",    ImageData.class,    resourcePackBoundDir);

		MODEL_SERIALIZER = serializerManager.getSerializer(ModelData.class);
		REGISTRY_SERIALIZER = serializerManager.getSerializer(RegistryData.class);
		MAPPING_SERIALIZER = serializerManager.getSerializer(MappingData.class);
		IMAGE_SERIALIZER = serializerManager.getSerializer(ImageData.class);
	}
}
