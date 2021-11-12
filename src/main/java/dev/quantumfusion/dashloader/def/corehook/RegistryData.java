package dev.quantumfusion.dashloader.def.corehook;

import dev.quantumfusion.dashloader.core.registry.ChunkHolder;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.core.registry.chunk.data.AbstractDataChunk;
import dev.quantumfusion.dashloader.def.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.def.data.blockstate.DashBlockState;
import dev.quantumfusion.dashloader.def.data.font.DashFont;
import dev.quantumfusion.dashloader.def.data.image.DashSprite;
import dev.quantumfusion.dashloader.def.data.model.components.DashBakedQuad;
import dev.quantumfusion.dashloader.def.data.model.predicates.DashPredicate;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.Font;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@Data
public class RegistryData implements ChunkHolder {
	public final AbstractDataChunk<BlockState, DashBlockState> blockStateRegistryData;
	public final AbstractDataChunk<Font, DashFont> fontRegistryData;
	public final AbstractDataChunk<Sprite, DashSprite> spriteRegistryData;
	public final AbstractDataChunk<Predicate<BlockState>, DashPredicate> predicateRegistryData;
/*	@Serialize(order = 8)
	@SerializeSubclasses(extraSubclassesId = "data", path = {0})
	public final List<DashDataClass> dataClassList;*/


	public RegistryData(AbstractDataChunk<BlockState, DashBlockState> blockStateRegistryData, AbstractDataChunk<Font, DashFont> fontRegistryData, AbstractDataChunk<Sprite, DashSprite> spriteRegistryData, AbstractDataChunk<Predicate<BlockState>, DashPredicate> predicateRegistryData) {
		this.blockStateRegistryData = blockStateRegistryData;
		this.fontRegistryData = fontRegistryData;
		this.spriteRegistryData = spriteRegistryData;
		this.predicateRegistryData = predicateRegistryData;
	}

	@SuppressWarnings("unchecked")
	public RegistryData(RegistryWriter writer) {
		this.blockStateRegistryData = writer.getChunk(DashBlockState.class).exportData();
		this.fontRegistryData = writer.getChunk(DashFont.class).exportData();
		this.spriteRegistryData = writer.getChunk(DashSprite.class).exportData();
		this.predicateRegistryData = writer.getChunk(DashPredicate.class).exportData();

		// TODO data classes
/*
		this.dataClassList = api.dataClasses;
*/
	}


	@Override
	public AbstractDataChunk<?, ?>[] getChunks() {
		return new AbstractDataChunk[]{blockStateRegistryData, fontRegistryData, spriteRegistryData, predicateRegistryData};
	}
}
