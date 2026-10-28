package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.util.math.AffineTransformation;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AffineTransformation.class)
public interface AffineTransformationAccessor {
	@Accessor("matrix")
	Matrix4fc getMatrix();
}
