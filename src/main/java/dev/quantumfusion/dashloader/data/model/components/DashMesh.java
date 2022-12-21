package dev.quantumfusion.dashloader.data.model.components;

import dev.quantumfusion.dashloader.util.ClassHelper;
import dev.quantumfusion.dashloader.util.UnsafeHelper;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;

import java.lang.reflect.Field;

public final class DashMesh {
	public final int[] data;
	public final String className;

	public DashMesh(int[] data, String className) {
		this.data = data;
		this.className = className;
	}

	public DashMesh(Mesh mesh) {
		this(getData(mesh), mesh.getClass().getName());
	}

	private static int[] getData(Mesh mesh) {
		final int[] data;
		try {
			final Field field = mesh.getClass().getDeclaredField("data");
			field.setAccessible(true);
			data = (int[]) field.get(mesh);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException("Could not use Mesh field hack. ", e);
		}
		return data;
	}


	public Mesh export() {
		final Class<?> aClass = ClassHelper.getClass(this.className);
		final Mesh mesh = (Mesh) UnsafeHelper.allocateInstance(aClass);
		try {
			assert aClass != null;
			final Field data = aClass.getDeclaredField("data");
			data.setAccessible(true);
			data.set(mesh, this.data);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException("Could not use Mesh field hack. ", e);
		}
		return mesh;
	}
}