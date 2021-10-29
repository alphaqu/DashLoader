package dev.quantumfusion.dashloader.def.data.dataobject;

import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.api.DashLoaderAPI;
import dev.quantumfusion.dashloader.def.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.def.data.blockstate.DashBlockState;
import dev.quantumfusion.dashloader.def.data.blockstate.property.DashProperty;
import dev.quantumfusion.dashloader.def.data.blockstate.property.value.DashPropertyValue;
import dev.quantumfusion.dashloader.def.data.font.DashFont;
import dev.quantumfusion.dashloader.def.data.image.DashSprite;
import dev.quantumfusion.dashloader.def.data.model.components.DashBakedQuad;
import dev.quantumfusion.dashloader.def.data.model.predicates.DashPredicate;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.Font;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.registry.export.ExportData;

import java.util.function.Function;
import java.util.function.Predicate;

@Data
public class RegistryData implements RegistryDataObject {
	public final ExportData<BlockState, DashBlockState> blockStateRegistryData;
	public final ExportData<Font, DashFont> fontRegistryData;
	public final ExportData<Identifier, DashIdentifierInterface> identifierRegistryData;
	public final ExportData<Property<?>, DashProperty> propertyRegistryData;
	public final ExportData<Comparable<?>, DashPropertyValue> propertyValueRegistryData;
	public final ExportData<Sprite, DashSprite> spriteRegistryData;
	public final ExportData<Predicate<BlockState>, DashPredicate> predicateRegistryData;
	public final ExportData<BakedQuad, DashBakedQuad> registryBakedQuadData;
/*	@Serialize(order = 8)
	@SerializeSubclasses(extraSubclassesId = "data", path = {0})
	public final List<DashDataClass> dataClassList;*/


	public RegistryData(ExportData<BlockState, DashBlockState> blockStateRegistryData,
			ExportData<Font, DashFont> fontRegistryData,
			ExportData<Identifier, DashIdentifierInterface> identifierRegistryData,
			ExportData<Property<?>, DashProperty> propertyRegistryData,
			ExportData<Comparable<?>, DashPropertyValue> propertyValueRegistryData,
			ExportData<Sprite, DashSprite> spriteRegistryData,
			ExportData<Predicate<BlockState>, DashPredicate> predicateRegistryData,
			ExportData<BakedQuad, DashBakedQuad> registryBakedQuadData
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
		Function<DashDataType, ExportData<?, ?>> getter = ((type) -> registry.getStorage(mappings.getByte(type)).getExportData());
		this.blockStateRegistryData = (ExportData<BlockState, DashBlockState>) getter.apply(DashDataType.BLOCKSTATE);
		this.fontRegistryData = (ExportData<Font, DashFont>) getter.apply(DashDataType.FONT);
		this.identifierRegistryData = (ExportData<Identifier, DashIdentifierInterface>) getter.apply(DashDataType.IDENTIFIER);
		this.propertyRegistryData = (ExportData<Property<?>, DashProperty>) getter.apply(DashDataType.PROPERTY);
		this.propertyValueRegistryData = (ExportData<Comparable<?>, DashPropertyValue>) getter.apply(DashDataType.PROPERTY_VALUE);
		this.spriteRegistryData = (ExportData<Sprite, DashSprite>) getter.apply(DashDataType.SPRITE);
		this.predicateRegistryData = (ExportData<Predicate<BlockState>, DashPredicate>) getter.apply(DashDataType.PREDICATE);
		this.registryBakedQuadData = (ExportData<BakedQuad, DashBakedQuad>) getter.apply(DashDataType.BAKEDQUAD);

		// TODO data classes
/*
		this.dataClassList = api.dataClasses;
*/
	}

	public void dumpData(DashExportHandler exportHandler) {
		exportHandler.addStorage(blockStateRegistryData);
		exportHandler.addStorage(fontRegistryData);
		exportHandler.addStorage(identifierRegistryData);
		exportHandler.addStorage(propertyRegistryData);
		exportHandler.addStorage(propertyValueRegistryData);
		exportHandler.addStorage(spriteRegistryData);
		exportHandler.addStorage(predicateRegistryData);
		exportHandler.addStorage(registryBakedQuadData);
	}

	@Override
	public int getSize() {
		return 8;
	}
}
