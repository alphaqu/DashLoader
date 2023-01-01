package dev.quantumfusion.dashloader.minecraft.model.components;

import dev.quantumfusion.dashloader.util.ClassHelper;
import dev.quantumfusion.dashloader.util.UnsafeHelper;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;

import java.lang.reflect.Field;
import java.util.Arrays;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashMesh dashMesh = (DashMesh) o;

		if (!Arrays.equals(data, dashMesh.data)) return false;
		return className.equals(dashMesh.className);
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(data);
		result = 31 * result + className.hashCode();
		return result;
	}
}