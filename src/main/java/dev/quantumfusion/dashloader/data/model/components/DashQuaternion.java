package dev.quantumfusion.dashloader.data.model.components;


import org.joml.Quaternionf;

public final class DashQuaternion {
	public final float x;
	public final float y;
	public final float z;
	public final float w;

	public DashQuaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public DashQuaternion(Quaternionf quaternion) {
		this(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
	}

	public Quaternionf export() {
		return new Quaternionf(this.x, this.y, this.z, this.w);
	}
}
