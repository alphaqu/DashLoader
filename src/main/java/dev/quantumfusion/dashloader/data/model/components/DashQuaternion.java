package dev.quantumfusion.dashloader.data.model.components;

import net.minecraft.util.math.Quaternion;

public record DashQuaternion(float x, float y, float z, float w) {
	public DashQuaternion(Quaternion quaternion) {
		this(quaternion.getX(), quaternion.getY(), quaternion.getZ(), quaternion.getW());
	}

	public Quaternion export() {
		return new Quaternion(this.x, this.y, this.z, this.w);
	}
}
