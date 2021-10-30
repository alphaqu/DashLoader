package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.dashloader.def.util.ClassHelper;
import dev.quantumfusion.dashloader.def.util.UnsafeHelper;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;

import java.lang.reflect.Field;
@Data
public record DashMesh(int[] data, String className) {

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
			throw new RuntimeException("shit", e);
		}
		return data;
	}


	public Mesh export() {
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