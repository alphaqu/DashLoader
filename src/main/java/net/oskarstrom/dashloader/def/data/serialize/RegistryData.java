package net.oskarstrom.dashloader.def.data.serialize;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeSubclasses;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import net.oskarstrom.dashloader.api.Dashable;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.registry.RegistryStorageManager;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.api.DashDataClass;
import net.oskarstrom.dashloader.def.api.DashDataType;
import net.oskarstrom.dashloader.def.api.DashLoaderAPI;
import net.oskarstrom.dashloader.def.blockstate.DashBlockState;
import net.oskarstrom.dashloader.def.blockstate.property.DashProperty;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashPropertyValue;
import net.oskarstrom.dashloader.def.data.DashIdentifierInterface;
import net.oskarstrom.dashloader.def.font.DashFont;
import net.oskarstrom.dashloader.def.image.DashSprite;
import net.oskarstrom.dashloader.def.model.components.DashBakedQuad;
import net.oskarstrom.dashloader.def.model.predicates.DashPredicate;

import java.util.List;
import java.util.function.Function;

public class RegistryData {
	@Serialize(order = 0)
	public final DashBlockState[] blockStateRegistryData;
	@Serialize(order = 1)
	public final DashFont[] fontRegistryData;
	@Serialize(order = 2)
	public final DashIdentifierInterface[] identifierRegistryData;
	@Serialize(order = 3)
	public final DashProperty[] propertyRegistryData;
	@Serialize(order = 4)
	public final DashPropertyValue[] propertyValueRegistryData;
	@Serialize(order = 5)
	public final DashSprite[] spriteRegistryData;
	@Serialize(order = 6)
	public final DashPredicate[] predicateRegistryData;
	@Serialize(order = 7)
	public final DashBakedQuad[] registryBakedQuadData;
	@Serialize(order = 8)
	@SerializeSubclasses(extraSubclassesId = "data", path = {0})
	public final List<DashDataClass> dataClassList;


	public RegistryData(@Deserialize("blockStateRegistryData") DashBlockState[] blockStateRegistryData,
						@Deserialize("fontRegistryData") DashFont[] fontRegistryData,
						@Deserialize("identifierRegistryData") DashIdentifierInterface[] identifierRegistryData,
						@Deserialize("propertyRegistryData") DashProperty[] propertyRegistryData,
						@Deserialize("propertyValueRegistryData") DashPropertyValue[] propertyValueRegistryData,
						@Deserialize("spriteRegistryData") DashSprite[] spriteRegistryData,
						@Deserialize("predicateRegistryData") DashPredicate[] predicateRegistryData,
						@Deserialize("registryBakedQuadData") DashBakedQuad[] registryBakedQuadData,
						@Deserialize("dataClassList") List<DashDataClass> dataClassList) {
		this.blockStateRegistryData = blockStateRegistryData;
		this.fontRegistryData = fontRegistryData;
		this.identifierRegistryData = identifierRegistryData;
		this.propertyRegistryData = propertyRegistryData;
		this.propertyValueRegistryData = propertyValueRegistryData;
		this.spriteRegistryData = spriteRegistryData;
		this.predicateRegistryData = predicateRegistryData;
		this.registryBakedQuadData = registryBakedQuadData;
		this.dataClassList = dataClassList;
	}

	public RegistryData(DashRegistry registry) {
		final DashLoaderAPI api = DashLoader.getInstance().getApi();
		final Object2ByteMap<DashDataType> mappings = api.storageMappings;
		Function<DashDataType, Dashable<?>[]> getter = ((type) -> registry.getStorage(mappings.getByte(type)).getDashables());

		this.blockStateRegistryData = (DashBlockState[]) getter.apply(DashDataType.BLOCKSTATE);
		this.fontRegistryData = (DashFont[]) getter.apply(DashDataType.FONT);
		this.identifierRegistryData = (DashIdentifierInterface[]) getter.apply(DashDataType.IDENTIFIER);
		this.propertyRegistryData = (DashProperty[]) getter.apply(DashDataType.PROPERTY);
		this.propertyValueRegistryData = (DashPropertyValue[]) getter.apply(DashDataType.PROPERTY_VALUE);
		this.spriteRegistryData = (DashSprite[]) getter.apply(DashDataType.SPRITE);
		this.predicateRegistryData = (DashPredicate[]) getter.apply(DashDataType.PREDICATE);
		this.registryBakedQuadData = (DashBakedQuad[]) getter.apply(DashDataType.BAKEDQUAD);

		// TODO data classes
		this.dataClassList = api.dataClasses;
	}

	public void dumpData(DashRegistry dashRegistry) {
		final RegistryStorageManager storageManager = DashLoader.getInstance().getCoreManager().getStorageManager();
		dashRegistry.addStorage(storageManager.createSupplierRegistry(blockStateRegistryData, dashRegistry));
		dashRegistry.addStorage(storageManager.createSupplierRegistry(fontRegistryData, dashRegistry));
		dashRegistry.addStorage(storageManager.createSupplierRegistry(identifierRegistryData, dashRegistry));
		dashRegistry.addStorage(storageManager.createSupplierRegistry(propertyRegistryData, dashRegistry));
		dashRegistry.addStorage(storageManager.createSupplierRegistry(propertyValueRegistryData, dashRegistry));
		dashRegistry.addStorage(storageManager.createSupplierRegistry(spriteRegistryData, dashRegistry));
		dashRegistry.addStorage(storageManager.createSupplierRegistry(predicateRegistryData, dashRegistry));
		dashRegistry.addStorage(storageManager.createSupplierRegistry(registryBakedQuadData, dashRegistry));
	}
}
