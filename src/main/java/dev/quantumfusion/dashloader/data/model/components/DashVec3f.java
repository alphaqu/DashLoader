package dev.quantumfusion.dashloader.data.model.components;

import net.minecraft.util.math.Vec3f;

public record DashVec3f(float x, float y, float z) {
	public DashVec3f(Vec3f vector3f) {
		this(vector3f.getX(), vector3f.getY(), vector3f.getZ());
	}

	public Vec3f export() {
		return new Vec3f(this.x, this.y, this.z);
	}


}
