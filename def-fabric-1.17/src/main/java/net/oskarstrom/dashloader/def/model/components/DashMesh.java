package net.oskarstrom.dashloader.def.model.components;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.oskarstrom.dashloader.def.util.ClassHelper;
import net.oskarstrom.dashloader.def.util.UnsafeHelper;


import java.lang.reflect.Field;

public class DashMesh {
	@Serialize(order = 0)
	public final int[] data;
	@Serialize(order = 1)
	public final String className;

	@SuppressWarnings("unused")
	public DashMesh(@Deserialize("data") int[] data,
					@Deserialize("className") String className) {
		this.data = data;
		this.className = className;
	}

	public DashMesh(Mesh mesh) {
		final Class<? extends Mesh> aClass = mesh.getClass();
		className = aClass.getName();
		try {
			final Field data = aClass.getDeclaredField("data");
			data.setAccessible(true);
			this.data = (int[]) data.get(mesh);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException("shit", e);
		}
	}

	public Mesh toUndash() {
		final Class<?> aClass = ClassHelper.getClass(className);
		final Mesh mesh = (Mesh) UnsafeHelper.allocateInstance(aClass);
		try {
			final Field data = aClass.getDeclaredField("data");
			data.setAccessible(true);
			data.set(mesh, this.data);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException("shit", e);
		}
		return mesh;
	}
}