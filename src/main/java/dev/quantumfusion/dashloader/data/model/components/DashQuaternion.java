package dev.quantumfusion.dashloader.data.model.components;

import net.minecraft.util.math.Quaternion;

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

	public DashQuaternion(Quaternion quaternion) {
		this(quaternion.getX(), quaternion.getY(), quaternion.getZ(), quaternion.getW());
	}

	public Quaternion export() {
		return new Quaternion(this.x, this.y, this.z, this.w);
	}
}
