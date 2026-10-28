package dev.notalpha.dashloader.mixin.option.misc;

import dev.notalpha.dashloader.mixin.accessor.AffineTransformationAccessor;
import net.minecraft.util.math.AffineTransformation;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(value = AffineTransformation.class, priority = 999)
public class AffineTransformationMixin {
	@Shadow
	@Final
	private Matrix4fc matrix;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AffineTransformation that = (AffineTransformation) o;
		return Objects.equals(matrix, ((AffineTransformationAccessor) (Object) that).getMatrix());
	}

	@Override
	public int hashCode() {
		return 31 + matrix.hashCode();
	}
}
