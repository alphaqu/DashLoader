package dev.quantumfusion.dashloader.data.model.components;

import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import org.jetbrains.annotations.Nullable;

@DataNullable
public final class DashModelTransformation {
	public final DashTransformation thirdPersonLeftHand;
	public final DashTransformation thirdPersonRightHand;
	public final DashTransformation firstPersonLeftHand;
	public final DashTransformation firstPersonRightHand;
	public final DashTransformation head;
	public final DashTransformation gui;
	public final DashTransformation ground;
	public final DashTransformation fixed;

	public transient int nullTransformations = 0;

	public DashModelTransformation(@Nullable DashTransformation thirdPersonLeftHand, @Nullable DashTransformation thirdPersonRightHand, @Nullable DashTransformation firstPersonLeftHand, @Nullable DashTransformation firstPersonRightHand, @Nullable DashTransformation head, @Nullable DashTransformation gui, @Nullable DashTransformation ground, @Nullable DashTransformation fixed) {
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

	@Nullable
	public static ModelTransformation exportOrDefault(@Nullable DashModelTransformation other) {
		if (other == null) {
			return ModelTransformation.NONE;
		}

		return other.export();
	}

	private DashTransformation createTransformation(Transformation transformation) {
		if (transformation == Transformation.IDENTITY) {
			this.nullTransformations++;
			return null;
		} else {
			return new DashTransformation(transformation);
		}
	}

	private Transformation unDashTransformation(DashTransformation transformation) {
		return transformation == null ? Transformation.IDENTITY : transformation.export();
	}

	public ModelTransformation export() {
		return new ModelTransformation(
				this.unDashTransformation(this.thirdPersonLeftHand),
				this.unDashTransformation(this.thirdPersonRightHand),
				this.unDashTransformation(this.firstPersonLeftHand),
				this.unDashTransformation(this.firstPersonRightHand),
				this.unDashTransformation(this.head),
				this.unDashTransformation(this.gui),
				this.unDashTransformation(this.ground),
				this.unDashTransformation(this.fixed)
		);
	}
}
