package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.util.math.Vec3f;

@Data
public record DashVec3f(float x, float y, float z) {
	public DashVec3f(Vec3f vector3f) {
		this(vector3f.getX(), vector3f.getY(), vector3f.getZ());
	}

	public Vec3f toUndash() {
		return new Vec3f(x, y, z);
	}


}
