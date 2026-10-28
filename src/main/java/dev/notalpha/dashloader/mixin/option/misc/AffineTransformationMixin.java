package dev.notalpha.dashloader.mixin.option.misc;

import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.mojang.math.Transformation;
import java.util.Objects;

@Mixin(value = Transformation.class, priority = 999)
public class AffineTransformationMixin {
	@Shadow
	@Final
	private Matrix4fc matrix;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AffineTransformationMixin that)) return false;
		if (!super.equals(o)) return false;

		return Objects.equals(matrix, that.matrix);
	}

	@Override
	public int hashCode() {
		return matrix.hashCode();
	}
}
