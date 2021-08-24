package net.oskarstrom.dashloader.def.data.serialize;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeSubclasses;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.registry.RegistryStorageData;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.api.DashDataType;
import net.oskarstrom.dashloader.def.api.DashLoaderAPI;
import net.oskarstrom.dashloader.def.blockstate.DashBlockState;
import net.oskarstrom.dashloader.def.blockstate.property.DashProperty;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashPropertyValue;
import net.oskarstrom.dashloader.def.common.DashIdentifierInterface;
import net.oskarstrom.dashloader.def.font.DashFont;
import net.oskarstrom.dashloader.def.image.DashSprite;
import net.oskarstrom.dashloader.def.model.components.DashBakedQuad;
import net.oskarstrom.dashloader.def.model.predicates.DashPredicate;

import java.util.function.Function;

public class RegistryData implements RegistryDataObject {
	@Serialize(order = 0)
	public final RegistryStorageData<DashBlockState> blockStateRegistryData;

	@Serialize(order = 1)
	@SerializeSubclasses(extraSubclassesId = "fonts", path = {0})
	public final RegistryStorageData<DashFont> fontRegistryData;

	@Serialize(order = 2)
	public final RegistryStorageData<DashIdentifierInterface> identifierRegistryData;

	@Serialize(order = 3)
	@SerializeSubclasses(extraSubclassesId = "properties", path = {0})
	public final RegistryStorageData<DashProperty> propertyRegistryData;

	@Serialize(order = 4)
	@SerializeSubclasses(extraSubclassesId = "values", path = {0})
	public final RegistryStorageData<DashPropertyValue> propertyValueRegistryData;

	@Serialize(order = 5)
	public final RegistryStorageData<DashSprite> spriteRegistryData;

	@Serialize(order = 6)
	@SerializeSubclasses(extraSubclassesId = "predicate", path = {0})
	public final RegistryStorageData<DashPredicate> predicateRegistryData;

	@Serialize(order = 7)
	public final RegistryStorageData<DashBakedQuad> registryBakedQuadData;
/*	@Serialize(order = 8)
	@SerializeSubclasses(extraSubclassesId = "data", path = {0})
	public final List<DashDataClass> dataClassList;*/


	public RegistryData(@Deserialize("blockStateRegistryData") RegistryStorageData<DashBlockState> blockStateRegistryData,
						@Deserialize("fontRegistryData") RegistryStorageData<DashFont> fontRegistryData,
						@Deserialize("identifierRegistryData") RegistryStorageData<DashIdentifierInterface> identifierRegistryData,
						@Deserialize("propertyRegistryData") RegistryStorageData<DashProperty> propertyRegistryData,
						@Deserialize("propertyValueRegistryData") RegistryStorageData<DashPropertyValue> propertyValueRegistryData,
						@Deserialize("spriteRegistryData") RegistryStorageData<DashSprite> spriteRegistryData,
						@Deserialize("predicateRegistryData") RegistryStorageData<DashPredicate> predicateRegistryData,
						@Deserialize("registryBakedQuadData") RegistryStorageData<DashBakedQuad> registryBakedQuadData
			/*				@Deserialize("dataClassList") List<DashDataClass> dataClassList*/) {
		this.blockStateRegistryData = blockStateRegistryData;
		this.fontRegistryData = fontRegistryData;
		this.identifierRegistryData = identifierRegistryData;
		this.propertyRegistryData = propertyRegistryData;
		this.propertyValueRegistryData = propertyValueRegistryData;
		this.spriteRegistryData = spriteRegistryData;
		this.predicateRegistryData = predicateRegistryData;
		this.registryBakedQuadData = registryBakedQuadData;
		/*		this.dataClassList = dataClassList;*/
	}

	@SuppressWarnings("unchecked")
	public RegistryData(DashRegistry registry) {
		final DashLoaderAPI api = DashLoader.getInstance().getApi();
		final Object2ByteMap<DashDataType> mappings = api.storageMappings;
		Function<DashDataType, RegistryStorageData<?>> getter = ((type) -> registry.getStorageData(mappings.getByte(type)));
		this.blockStateRegistryData = (RegistryStorageData<DashBlockState>) getter.apply(DashDataType.BLOCKSTATE);
		this.fontRegistryData = (RegistryStorageData<DashFont>) getter.apply(DashDataType.FONT);
		this.identifierRegistryData = (RegistryStorageData<DashIdentifierInterface>) getter.apply(DashDataType.IDENTIFIER);
		this.propertyRegistryData = (RegistryStorageData<DashProperty>) getter.apply(DashDataType.PROPERTY);
		this.propertyValueRegistryData = (RegistryStorageData<DashPropertyValue>) getter.apply(DashDataType.PROPERTY_VALUE);
		this.spriteRegistryData = (RegistryStorageData<DashSprite>) getter.apply(DashDataType.SPRITE);
		this.predicateRegistryData = (RegistryStorageData<DashPredicate>) getter.apply(DashDataType.PREDICATE);
		this.registryBakedQuadData = (RegistryStorageData<DashBakedQuad>) getter.apply(DashDataType.BAKEDQUAD);

		// TODO data classes
/*
		this.dataClassList = api.dataClasses;
*/
	}

	public void dumpData(DashRegistry dashRegistry) {
		dashRegistry.addStorage(blockStateRegistryData);
		dashRegistry.addStorage(fontRegistryData);
		dashRegistry.addStorage(identifierRegistryData);
		dashRegistry.addStorage(propertyRegistryData);
		dashRegistry.addStorage(propertyValueRegistryData);
		dashRegistry.addStorage(spriteRegistryData);
		dashRegistry.addStorage(predicateRegistryData);
		dashRegistry.addStorage(registryBakedQuadData);
	}

	@Override
	public int getSize() {
		return 8;
	}
}
