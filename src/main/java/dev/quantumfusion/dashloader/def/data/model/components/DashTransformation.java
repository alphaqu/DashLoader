package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.json.Transformation;

@Data
public record DashTransformation(DashVec3f rotation, DashVec3f translation, DashVec3f scale) {
	public DashTransformation(Transformation transformation) {
		this(new DashVec3f(transformation.rotation), new DashVec3f(transformation.translation), new DashVec3f(transformation.scale));
	}

	public Transformation export() {
		return new Transformation(rotation.export(), translation.export(), scale.export());
	}
}
