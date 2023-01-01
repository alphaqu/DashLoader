package dev.notalpha.dashloader.minecraft.model.components;

import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import org.jetbrains.annotations.Nullable;

@DataNullable
public final class DashModelTransformation {
	public final Transformation thirdPersonLeftHand;
	public final Transformation thirdPersonRightHand;
	public final Transformation firstPersonLeftHand;
	public final Transformation firstPersonRightHand;
	public final Transformation head;
	public final Transformation gui;
	public final Transformation ground;
	public final Transformation fixed;

	public transient int nullTransformations = 0;

	public DashModelTransformation(@Nullable Transformation thirdPersonLeftHand, @Nullable Transformation thirdPersonRightHand, @Nullable Transformation firstPersonLeftHand, @Nullable Transformation firstPersonRightHand, @Nullable Transformation head, @Nullable Transformation gui, @Nullable Transformation ground, @Nullable Transformation fixed) {
		this.thirdPersonLeftHand = thirdPersonLeftHand;
		this.thirdPersonRightHand = thirdPersonRightHand;
		this.firstPersonLeftHand = firstPersonLeftHand;
		this.firstPersonRightHand = firstPersonRightHand;
		this.head = head;
		this.gui = gui;
		this.ground = ground;
		this.fixed = fixed;
	}

	public DashModelTransformation(ModelTransformation other) {
		this.thirdPersonLeftHand = this.createTransformation(other.thirdPersonLeftHand);
		this.thirdPersonRightHand = this.createTransformation(other.thirdPersonRightHand);
		this.firstPersonLeftHand = this.createTransformation(other.firstPersonLeftHand);
		this.firstPersonRightHand = this.createTransformation(other.firstPersonRightHand);
		this.head = this.createTransformation(other.head);
		this.gui = this.createTransformation(other.gui);
		this.ground = this.createTransformation(other.ground);
		this.fixed = this.createTransformation(other.fixed);
	}

	@Nullable
	public static DashModelTransformation createDashOrReturnNullIfDefault(ModelTransformation other) {
		if (other == ModelTransformation.NONE) {
			return null;
		}

		DashModelTransformation out = new DashModelTransformation(other);

		if (out.nullTransformations == 8) {
			return null;
		}

		return out;
	}

	public static ModelTransformation exportOrDefault(@Nullable DashModelTransformation other) {
		if (other == null) {
			return ModelTransformation.NONE;
		}

		return other.export();
	}

	private Transformation createTransformation(Transformation transformation) {
		if (transformation == Transformation.IDENTITY) {
			this.nullTransformations++;
			return null;
		} else {
			return transformation;
		}
	}

	private Transformation unTransformation(Transformation transformation) {
		return transformation == null ? Transformation.IDENTITY : transformation;
	}

	public ModelTransformation export() {
		return new ModelTransformation(
				this.unTransformation(this.thirdPersonLeftHand),
				this.unTransformation(this.thirdPersonRightHand),
				this.unTransformation(this.firstPersonLeftHand),
				this.unTransformation(this.firstPersonRightHand),
				this.unTransformation(this.head),
				this.unTransformation(this.gui),
				this.unTransformation(this.ground),
				this.unTransformation(this.fixed)
		);
	}
}
