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
import net.oskarstrom.dashloader.def.common.DashIdentifier;
import net.oskarstrom.dashloader.def.common.DashIdentifierInterface;
import net.oskarstrom.dashloader.def.common.DashModelIdentifier;
import net.oskarstrom.dashloader.def.font.*;
import net.oskarstrom.dashloader.def.image.DashSprite;
import net.oskarstrom.dashloader.def.model.components.DashBakedQuad;
import net.oskarstrom.dashloader.def.model.predicates.DashPredicate;

import java.util.function.Function;

public class RegistryData implements RegistryDataObject {
	@Serialize
	public final RegistryStorageData<DashBlockState> blockStateRegistryData;

	@Serialize
	public final RegistryStorageData<@SerializeSubclasses(extraSubclassesId = "fonts") DashFont> fontRegistryData;

	@Serialize
	public final RegistryStorageData<@SerializeSubclasses({DashIdentifier.class, DashModelIdentifier.class}) DashIdentifierInterface> identifierRegistryData;

	@Serialize
	public final RegistryStorageData<@SerializeSubclasses(extraSubclassesId = "properties") DashProperty> propertyRegistryData;

	@Serialize
	public final RegistryStorageData<@SerializeSubclasses(extraSubclassesId = "values") DashPropertyValue> propertyValueRegistryData;

	@Serialize
	public final RegistryStorageData<DashSprite> spriteRegistryData;

	@Serialize
	public final RegistryStorageData<@SerializeSubclasses(extraSubclassesId = "predicate") DashPredicate> predicateRegistryData;

	@Serialize
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
		Function<DashDataType, RegistryStorageData<?>> getter = ((type) -> {
			final RegistryStorageData<?> storageData = registry.getStorageData(mappings.getByte(type));
			System.out.println(type.name + " / " + storageData.dashables.size());
			return storageData;
		});
		this.blockStateRegistryData = (RegistryStorageData<DashBlockState>) getter.apply(DashDataType.BLOCKSTATE);
		this.fontRegistryData = (RegistryStorageData<DashFont>) getter.apply(DashDataType.FONT);

		for (DashFont dashable : fontRegistryData.dashables) {
			final Class<? extends DashFont> aClass = dashable.getClass();
			if (!(aClass == DashBitmapFont.class || aClass == DashBlankFont.class || aClass == DashTrueTypeFont.class || aClass == DashUnicodeFont.class)) {
				System.out.println(aClass);
			}
		}
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
