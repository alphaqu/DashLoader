package dev.quantumfusion.dashloader.data.registry;

import dev.quantumfusion.dashloader.data.blockstate.DashBlockState;
import dev.quantumfusion.dashloader.data.font.DashFont;
import dev.quantumfusion.dashloader.data.image.DashSprite;
import dev.quantumfusion.dashloader.data.model.predicates.DashPredicate;
import dev.quantumfusion.dashloader.registry.ChunkHolder;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.chunk.data.AbstractDataChunk;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.Font;
import net.minecraft.client.texture.Sprite;

public class RegistryData implements ChunkHolder {
	public final AbstractDataChunk<BlockState, DashBlockState> blockStateRegistryData;
	public final AbstractDataChunk<Font, DashFont> fontRegistryData;
	public final AbstractDataChunk<Sprite, DashSprite> spriteRegistryData;
	public final AbstractDataChunk<Predicate<BlockState>, DashPredicate> predicateRegistryData;

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
	}


	@Override
	public AbstractDataChunk<?, ?>[] getChunks() {
		return new AbstractDataChunk[]{this.blockStateRegistryData, this.fontRegistryData, this.spriteRegistryData, this.predicateRegistryData};
	}
}
