package dev.quantumfusion.dashloader.mixin.accessor;

import java.util.List;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.WeightedBakedModel;
import net.minecraft.util.collection.Weighted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WeightedBakedModel.class)
public interface WeightedBakedModelAccessor {

	@Accessor("models")
	List<Weighted.Present<BakedModel>> getBakedModels();

	@Accessor
	void setModels(List<Weighted.Present<BakedModel>> models);
}
