package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.util.math.Quaternion;

@Data
public record DashQuaternion(float x, float y, float z, float w) {
	public DashQuaternion(Quaternion quaternion) {
		this(quaternion.getX(), quaternion.getY(), quaternion.getZ(), quaternion.getW());
	}

	public Quaternion toUndash() {
		return new Quaternion(x, y, z, w);
	}
}
