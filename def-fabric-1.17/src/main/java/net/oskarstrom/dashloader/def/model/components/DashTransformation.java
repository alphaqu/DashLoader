package net.oskarstrom.dashloader.def.model.components;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.render.model.json.Transformation;

public class DashTransformation {
	@Serialize(order = 0)
	public final DashVec3f rotation;
	@Serialize(order = 1)
	public final DashVec3f translation;
	@Serialize(order = 2)
	public final DashVec3f scale;

	public DashTransformation(@Deserialize("rotation") DashVec3f rotation,
							  @Deserialize("translation") DashVec3f translation,
							  @Deserialize("scale") DashVec3f scale) {
		this.rotation = rotation;
		this.translation = translation;
		this.scale = scale;
	}

	public DashTransformation(Transformation transformation) {
		rotation = new DashVec3f(transformation.rotation);
		translation = new DashVec3f(transformation.translation);
		scale = new DashVec3f(transformation.scale);
	}

	public Transformation toUndash() {
		return new Transformation(rotation.toUndash(), translation.toUndash(), scale.toUndash());
	}
}
