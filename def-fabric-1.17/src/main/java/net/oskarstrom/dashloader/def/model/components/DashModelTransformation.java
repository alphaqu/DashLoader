package net.oskarstrom.dashloader.def.model.components;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import org.jetbrains.annotations.Nullable;

public class DashModelTransformation {

	@Serialize(order = 0)
	@SerializeNullable
	@Nullable
	public final DashTransformation thirdPersonLeftHand;
	@Serialize(order = 1)
	@SerializeNullable
	@Nullable
	public final DashTransformation thirdPersonRightHand;
	@Serialize(order = 2)
	@SerializeNullable
	@Nullable
	public final DashTransformation firstPersonLeftHand;
	@Serialize(order = 3)
	@SerializeNullable
	@Nullable
	public final DashTransformation firstPersonRightHand;
	@Serialize(order = 4)
	@SerializeNullable
	@Nullable
	public final DashTransformation head;
	@Serialize(order = 5)
	@SerializeNullable
	@Nullable
	public final DashTransformation gui;
	@Serialize(order = 6)
	@SerializeNullable
	@Nullable
	public final DashTransformation ground;
	@Serialize(order = 7)
	@SerializeNullable
	@Nullable
	public final DashTransformation fixed;


	public int nullTransformations = 0;


	public DashModelTransformation(@Deserialize("thirdPersonLeftHand") @Nullable DashTransformation thirdPersonLeftHand,
								   @Deserialize("thirdPersonRightHand") @Nullable DashTransformation thirdPersonRightHand,
								   @Deserialize("firstPersonLeftHand") @Nullable DashTransformation firstPersonLeftHand,
								   @Deserialize("firstPersonRightHand") @Nullable DashTransformation firstPersonRightHand,
								   @Deserialize("head") @Nullable DashTransformation head,
								   @Deserialize("gui") @Nullable DashTransformation gui,
								   @Deserialize("ground") @Nullable DashTransformation ground,
								   @Deserialize("fixed") @Nullable DashTransformation fixed
	) {
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

	public static DashModelTransformation createDashModelTransformation(ModelTransformation other) {
		DashModelTransformation out = new DashModelTransformation(other);
		if (out.nullTransformations == 8) {
			return null;
		}
		return out;
	}

	private DashTransformation createTransformation(Transformation transformation) {
		final DashTransformation dashTransformation = transformation == Transformation.IDENTITY ? null : new DashTransformation(transformation);
		if (dashTransformation == null) {
			nullTransformations++;
		}
		return dashTransformation;
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
