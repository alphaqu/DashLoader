package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.client.render.model.WeightedBlockStateModel;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.util.collection.WeightedPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WeightedBlockStateModel.class)
public interface WeightedBlockStateModelAccessor {
	@Accessor("models")
	WeightedPool<BlockStateModel> getModels();
}
