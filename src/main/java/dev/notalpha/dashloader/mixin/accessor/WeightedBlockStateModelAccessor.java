package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.WeightedVariants;
import net.minecraft.util.random.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WeightedVariants.class)
public interface WeightedBlockStateModelAccessor {
	@Accessor("list")
	WeightedList<BlockStateModel> getModels();
}
