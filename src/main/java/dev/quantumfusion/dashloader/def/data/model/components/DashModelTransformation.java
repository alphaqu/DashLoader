package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import org.jetbrains.annotations.Nullable;

@Data
@DataNullable
public class DashModelTransformation {
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
		this.thirdPersonLeftHand = createTransformation(other.thirdPersonLeftHand);
		this.thirdPersonRightHand = createTransformation(other.thirdPersonRightHand);
		this.firstPersonLeftHand = createTransformation(other.firstPersonLeftHand);
		this.firstPersonRightHand = createTransformation(other.firstPersonRightHand);
		this.head = createTransformation(other.head);
		this.gui = createTransformation(other.gui);
		this.ground = createTransformation(other.ground);
		this.fixed = createTransformation(other.fixed);
	}

	@Nullable
	public static DashModelTransformation createDashOrReturnNullIfDefault(ModelTransformation other) {
		if (other == ModelTransformation.NONE)
			return null;

		DashModelTransformation out = new DashModelTransformation(other);

		if (out.nullTransformations == 8)
			return null;

		return out;
	}

	@Nullable
	public static ModelTransformation toUndashOrDefault(@Nullable DashModelTransformation other) {
		if (other == null)
			return ModelTransformation.NONE;

		return other.toUndash();
	}

	private DashTransformation createTransformation(Transformation transformation) {
		if (transformation == Transformation.IDENTITY) {
			nullTransformations++;
			return null;
		} else {
			return new DashTransformation(transformation);
		}
	}

	private Transformation unDashTransformation(DashTransformation transformation) {
		return transformation == null ? Transformation.IDENTITY : transformation.toUndash();
	}

	public ModelTransformation toUndash() {
		return new ModelTransformation(
				unDashTransformation(thirdPersonLeftHand),
				unDashTransformation(thirdPersonRightHand),
				unDashTransformation(firstPersonLeftHand),
				unDashTransformation(firstPersonRightHand),
				unDashTransformation(head),
				unDashTransformation(gui),
				unDashTransformation(ground),
				unDashTransformation(fixed)
		);
	}
}
