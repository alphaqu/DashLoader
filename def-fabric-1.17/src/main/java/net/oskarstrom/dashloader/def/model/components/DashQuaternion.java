package net.oskarstrom.dashloader.def.model.components;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.util.math.Quaternion;

public class DashQuaternion {

	@Serialize(order = 0)
	public final float x;
	@Serialize(order = 1)
	public final float y;
	@Serialize(order = 2)
	public final float z;
	@Serialize(order = 3)
	public final float w;

	public DashQuaternion(@Deserialize("x") float x,
						  @Deserialize("y") float y,
						  @Deserialize("z") float z,
						  @Deserialize("w") float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public DashQuaternion(Quaternion quaternion) {
		x = quaternion.getX();
		y = quaternion.getY();
		z = quaternion.getZ();
		w = quaternion.getW();
	}

	public Quaternion toUndash() {
		return new Quaternion(x, y, z, w);
	}
}
