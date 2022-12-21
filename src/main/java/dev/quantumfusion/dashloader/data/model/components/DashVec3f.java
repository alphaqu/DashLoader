package dev.quantumfusion.dashloader.data.model.components;


import org.joml.Vector3f;

public final class DashVec3f {
	public final float x;
	public final float y;
	public final float z;

	public DashVec3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public DashVec3f(Vector3f vector3f) {
		this(vector3f.x, vector3f.y, vector3f.z);
	}

	public Vector3f export() {
		return new Vector3f(this.x, this.y, this.z);
	}
}
