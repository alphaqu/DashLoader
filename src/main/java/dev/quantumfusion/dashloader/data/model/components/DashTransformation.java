package dev.quantumfusion.dashloader.data.model.components;

import net.minecraft.client.render.model.json.Transformation;

public final class DashTransformation {
	public final DashVec3f rotation;
	public final DashVec3f translation;
	public final DashVec3f scale;

	public DashTransformation(DashVec3f rotation, DashVec3f translation, DashVec3f scale) {
		this.rotation = rotation;
		this.translation = translation;
		this.scale = scale;
	}

	public DashTransformation(Transformation transformation) {
		this(new DashVec3f(transformation.rotation), new DashVec3f(transformation.translation), new DashVec3f(transformation.scale));
	}

	public Transformation export() {
		return new Transformation(this.rotation.export(), this.translation.export(), this.scale.export());
	}
}
