package net.oskarstrom.dashloader.def.data.serialize;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeSubclasses;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.Font;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.registry.RegistryStorageDataImpl;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.api.DashDataType;
import net.oskarstrom.dashloader.def.api.DashLoaderAPI;
import net.oskarstrom.dashloader.def.blockstate.DashBlockState;
import net.oskarstrom.dashloader.def.blockstate.property.DashProperty;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashPropertyValue;
import net.oskarstrom.dashloader.def.common.DashIdentifier;
import net.oskarstrom.dashloader.def.common.DashIdentifierInterface;
import net.oskarstrom.dashloader.def.common.DashModelIdentifier;
import net.oskarstrom.dashloader.def.font.DashFont;
import net.oskarstrom.dashloader.def.image.DashImage;
import net.oskarstrom.dashloader.def.image.DashSprite;
import net.oskarstrom.dashloader.def.model.components.DashBakedQuad;
import net.oskarstrom.dashloader.def.model.predicates.DashPredicate;

import java.util.function.Function;
import java.util.function.Predicate;

public class RegistryData implements RegistryDataObject {
	@Serialize
	public final RegistryStorageDataImpl<BlockState, DashBlockState> blockStateRegistryData;

	@Serialize
	@SerializeSubclasses(extraSubclassesId = "fonts", path = {1})
	public final RegistryStorageDataImpl<Font, DashFont> fontRegistryData;

	@Serialize
	@SerializeSubclasses(value = {DashIdentifier.class, DashModelIdentifier.class}, path = {1})
	public final RegistryStorageDataImpl<Identifier, DashIdentifierInterface> identifierRegistryData;

	@Serialize
	@SerializeSubclasses(extraSubclassesId = "properties", path = {1})
	public final RegistryStorageDataImpl<Property<?>, DashProperty> propertyRegistryData;

	@Serialize
	@SerializeSubclasses(extraSubclassesId = "values", path = {1})
	public final RegistryStorageDataImpl<Comparable<?>, DashPropertyValue> propertyValueRegistryData;

	@Serialize
	public final RegistryStorageDataImpl<Sprite, DashSprite> spriteRegistryData;

	@Serialize
	@SerializeSubclasses(extraSubclassesId = "predicates", path = {0})
	public final RegistryStorageDataImpl<Predicate<BlockState>, DashPredicate> predicateRegistryData;

	@Serialize
	public final RegistryStorageDataImpl<BakedQuad, DashBakedQuad> registryBakedQuadData;
/*	@Serialize(order = 8)
	@SerializeSubclasses(extraSubclassesId = "data", path = {0})
	public final List<DashDataClass> dataClassList;*/


	public RegistryData(@Deserialize("blockStateRegistryData") RegistryStorageDataImpl<BlockState, DashBlockState> blockStateRegistryData,
						@Deserialize("fontRegistryData") RegistryStorageDataImpl<Font, DashFont> fontRegistryData,
						@Deserialize("identifierRegistryData") RegistryStorageDataImpl<Identifier, DashIdentifierInterface> identifierRegistryData,
						@Deserialize("propertyRegistryData") RegistryStorageDataImpl<Property<?>, DashProperty> propertyRegistryData,
						@Deserialize("propertyValueRegistryData") RegistryStorageDataImpl<Comparable<?>, DashPropertyValue> propertyValueRegistryData,
						@Deserialize("spriteRegistryData") RegistryStorageDataImpl<Sprite, DashSprite> spriteRegistryData,
						@Deserialize("predicateRegistryData") RegistryStorageDataImpl<Predicate<BlockState>, DashPredicate> predicateRegistryData,
						@Deserialize("registryBakedQuadData") RegistryStorageDataImpl<BakedQuad, DashBakedQuad> registryBakedQuadData
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
		Function<DashDataType, RegistryStorageDataImpl<?, ?>> getter = ((type) -> {
			byte pos = mappings.getByte(type);
			var storage = registry.getStorage(pos);
			return new RegistryStorageDataImpl<>((DashImage[]) storage.getDashables(), pos, (short) 0);
		});
		this.blockStateRegistryData = (RegistryStorageDataImpl<BlockState, DashBlockState>) getter.apply(DashDataType.BLOCKSTATE);
		this.fontRegistryData = (RegistryStorageDataImpl<Font, DashFont>) getter.apply(DashDataType.FONT);
		this.identifierRegistryData = (RegistryStorageDataImpl<Identifier, DashIdentifierInterface>) getter.apply(DashDataType.IDENTIFIER);
		this.propertyRegistryData = (RegistryStorageDataImpl<Property<?>, DashProperty>) getter.apply(DashDataType.PROPERTY);
		this.propertyValueRegistryData = (RegistryStorageDataImpl<Comparable<?>, DashPropertyValue>) getter.apply(DashDataType.PROPERTY_VALUE);
		this.spriteRegistryData = (RegistryStorageDataImpl<Sprite, DashSprite>) getter.apply(DashDataType.SPRITE);
		this.predicateRegistryData = (RegistryStorageDataImpl<Predicate<BlockState>, DashPredicate>) getter.apply(DashDataType.PREDICATE);
		this.registryBakedQuadData = (RegistryStorageDataImpl<BakedQuad, DashBakedQuad>) getter.apply(DashDataType.BAKEDQUAD);

		// TODO data classes
/*
		this.dataClassList = api.dataClasses;
*/
	}

	public void dumpData(DashExportHandler exportHandler) {
		exportHandler.addStorage(blockStateRegistryData, blockStateRegistryData.registryPos);
		exportHandler.addStorage(fontRegistryData, fontRegistryData.registryPos);
		exportHandler.addStorage(identifierRegistryData, identifierRegistryData.registryPos);
		exportHandler.addStorage(propertyRegistryData, propertyRegistryData.registryPos);
		exportHandler.addStorage(propertyValueRegistryData, propertyValueRegistryData.registryPos);
		exportHandler.addStorage(spriteRegistryData, spriteRegistryData.registryPos);
		exportHandler.addStorage(predicateRegistryData, predicateRegistryData.registryPos);
		exportHandler.addStorage(registryBakedQuadData, registryBakedQuadData.registryPos);
	}

	@Override
	public int getSize() {
		return 8;
	}
}
