package net.oskarstrom.dashloader.def.data;

import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.data.serialize.ImageData;
import net.oskarstrom.dashloader.def.data.serialize.MappingData;
import net.oskarstrom.dashloader.def.data.serialize.ModelData;
import net.oskarstrom.dashloader.def.data.serialize.RegistryData;
import net.oskarstrom.dashloader.def.util.enums.DashCachePaths;
import net.oskarstrom.dashloader.api.serializer.DashSerializer;
import net.oskarstrom.dashloader.api.serializer.DashSerializerManager;

public class DashSerializers {
	public static DashSerializer<ModelData> MODEL_SERIALIZER;
	public static DashSerializer<RegistryData> REGISTRY_SERIALIZER;
	public static DashSerializer<MappingData> MAPPING_SERIALIZER;
	public static DashSerializer<ImageData> IMAGE_SERIALIZER;

	public static void initSerializers() {
		final DashSerializerManager serializerManager = DashLoader.getInstance().getCoreManager().getSerializerManager();
		serializerManager.loadOrCreateSerializer("model",    ModelData.class,    DashCachePaths.REGISTRY_MODEL_CACHE.getPath(), "models");
		serializerManager.loadOrCreateSerializer("registry", RegistryData.class, DashCachePaths.REGISTRY_CACHE.getPath(), "predicates", "fonts", "properties", "values", "data");
		serializerManager.loadOrCreateSerializer("mapping",  MappingData.class,  DashCachePaths.MAPPINGS_CACHE.getPath());
		serializerManager.loadOrCreateSerializer("image",    ImageData.class,    DashCachePaths.REGISTRY_IMAGE_CACHE.getPath());

		MODEL_SERIALIZER = serializerManager.getSerializer(ModelData.class);
		REGISTRY_SERIALIZER = serializerManager.getSerializer(RegistryData.class);
		MAPPING_SERIALIZER = serializerManager.getSerializer(MappingData.class);
		IMAGE_SERIALIZER = serializerManager.getSerializer(ImageData.class);
	}
}
