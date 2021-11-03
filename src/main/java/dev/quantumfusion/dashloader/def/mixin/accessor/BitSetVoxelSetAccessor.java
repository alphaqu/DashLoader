package dev.quantumfusion.dashloader.def.mixin.accessor;

import net.minecraft.util.shape.BitSetVoxelSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BitSetVoxelSet.class)
public interface BitSetVoxelSetAccessor {
	@Invoker("method_31942")
	void cleanColumn(int z1, int z2, int x, int y);

	@Invoker("isColumnFull")
	boolean isColumnFullInvoke(int i, int j, int k, int l);

	@Invoker("method_31938")
	boolean isTableFull(int xStart, int xStop , int zStart, int zEnd, int y);

}
