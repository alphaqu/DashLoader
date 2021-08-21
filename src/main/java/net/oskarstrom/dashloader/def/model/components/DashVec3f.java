package net.oskarstrom.dashloader.def.model.components;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.util.math.Vec3f;

public class DashVec3f {

	@Serialize(order = 0)
	public final float x;
	@Serialize(order = 1)
	public final float y;
	@Serialize(order = 2)
	public final float z;


	public DashVec3f(@Deserialize("x") float x, @Deserialize("y") float y, @Deserialize("z") float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public DashVec3f(Vec3f vector3f) {
		x = vector3f.getX();
		y = vector3f.getY();
		z = vector3f.getZ();
	}

	public Vec3f toUndash() {
		return new Vec3f(x, y, z);
	}


}
