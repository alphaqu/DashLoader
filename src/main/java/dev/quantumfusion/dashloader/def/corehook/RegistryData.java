package dev.quantumfusion.dashloader.def.corehook;

import dev.quantumfusion.dashloader.core.registry.ChunkDataHolder;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.core.registry.chunk.data.AbstractDataChunk;
import dev.quantumfusion.dashloader.def.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.def.data.blockstate.DashBlockState;
import dev.quantumfusion.dashloader.def.data.blockstate.property.DashProperty;
import dev.quantumfusion.dashloader.def.data.blockstate.property.value.DashPropertyValue;
import dev.quantumfusion.dashloader.def.data.font.DashFont;
import dev.quantumfusion.dashloader.def.data.image.DashSprite;
import dev.quantumfusion.dashloader.def.data.model.components.DashBakedQuad;
import dev.quantumfusion.dashloader.def.data.model.predicates.DashPredicate;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.Font;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@Data
public class RegistryData implements ChunkDataHolder {
	public final AbstractDataChunk<BlockState, DashBlockState> blockStateRegistryData;
	public final AbstractDataChunk<Font, DashFont> fontRegistryData;
	public final AbstractDataChunk<Identifier, DashIdentifierInterface> identifierRegistryData;
	public final AbstractDataChunk<Property<?>, DashProperty> propertyRegistryData;
	public final AbstractDataChunk<Comparable<?>, DashPropertyValue> propertyValueRegistryData;
	public final AbstractDataChunk<Sprite, DashSprite> spriteRegistryData;
	public final AbstractDataChunk<Predicate<BlockState>, DashPredicate> predicateRegistryData;
	public final AbstractDataChunk<BakedQuad, DashBakedQuad> registryBakedQuadData;
/*	@Serialize(order = 8)
	@SerializeSubclasses(extraSubclassesId = "data", path = {0})
	public final List<DashDataClass> dataClassList;*/


	public RegistryData(AbstractDataChunk<BlockState, DashBlockState> blockStateRegistryData, AbstractDataChunk<Font, DashFont> fontRegistryData, AbstractDataChunk<Identifier, DashIdentifierInterface> identifierRegistryData, AbstractDataChunk<Property<?>, DashProperty> propertyRegistryData, AbstractDataChunk<Comparable<?>, DashPropertyValue> propertyValueRegistryData, AbstractDataChunk<Sprite, DashSprite> spriteRegistryData, AbstractDataChunk<Predicate<BlockState>, DashPredicate> predicateRegistryData, AbstractDataChunk<BakedQuad, DashBakedQuad> registryBakedQuadData) {
		this.blockStateRegistryData = blockStateRegistryData;
		this.fontRegistryData = fontRegistryData;
		this.identifierRegistryData = identifierRegistryData;
		this.propertyRegistryData = propertyRegistryData;
		this.propertyValueRegistryData = propertyValueRegistryData;
		this.spriteRegistryData = spriteRegistryData;
		this.predicateRegistryData = predicateRegistryData;
		this.registryBakedQuadData = registryBakedQuadData;
	}

	@SuppressWarnings("unchecked")
	public RegistryData(DashRegistryWriter writer) {
		this.blockStateRegistryData = writer.getChunk(DashBlockState.class).exportData();
		this.fontRegistryData = writer.getChunk(DashFont.class).exportData();
		this.identifierRegistryData = writer.getChunk(DashIdentifierInterface.class).exportData();
		this.propertyRegistryData = writer.getChunk(DashProperty.class).exportData();
		this.propertyValueRegistryData = writer.getChunk(DashPropertyValue.class).exportData();
		this.spriteRegistryData = writer.getChunk(DashSprite.class).exportData();
		this.predicateRegistryData = writer.getChunk(DashPredicate.class).exportData();
		this.registryBakedQuadData = writer.getChunk(DashBakedQuad.class).exportData();

		// TODO data classes
/*
		this.dataClassList = api.dataClasses;
*/
	}


	@Override
	public Collection<AbstractDataChunk<?, ?>> getChunks() {
		return List.of(blockStateRegistryData, fontRegistryData, identifierRegistryData, propertyRegistryData, propertyValueRegistryData, spriteRegistryData, predicateRegistryData, registryBakedQuadData);
	}
}
