package dev.quantumfusion.dashloader.data.model.components;

import net.minecraft.util.math.Vec3f;

public final class DashVec3f {
	public final float x;
	public final float y;
	public final float z;

	public DashVec3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public DashVec3f(Vec3f vector3f) {
		this(vector3f.getX(), vector3f.getY(), vector3f.getZ());
	}

	public Vec3f export() {
		return new Vec3f(this.x, this.y, this.z);
	}
}
