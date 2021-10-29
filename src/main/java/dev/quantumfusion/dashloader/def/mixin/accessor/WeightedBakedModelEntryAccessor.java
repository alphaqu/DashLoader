package dev.quantumfusion.dashloader.def.mixin.accessor;

import net.minecraft.util.collection.Weight;
import net.minecraft.util.collection.Weighted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Weighted.Present.class)
public interface WeightedBakedModelEntryAccessor {

	@Invoker("<init>")
	static Weighted.Present init(Object data, Weight weight) {
		throw new AssertionError();
	}

	@Accessor
	Object getData();
}
