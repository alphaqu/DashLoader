package dev.notalpha.dashloader.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * A DashObject which is an object with adds Dash support to a target object. <br>
 * This class is very lazy as reflection is really slow
 *
 * @param <R> Raw
 * @param <D> Dashable
 */
public final class DashObjectClass<R, D extends Dashable<R>> {
	private final Class<D> dashClass;
	@Nullable
	private Class<R> targetClass;
	int dashObjectId;
	public DashObjectClass(Class<?> dashClass) {
		//noinspection unchecked
		this.dashClass = (Class<D>) dashClass;
	}

	public Class<D> getDashClass() {
		return this.dashClass;
	}

	// lazy
	@NotNull
	public Class<R> getTargetClass() {
		if (this.targetClass == null) {
			resolveDashObjectAnnotation();
		}
		return this.targetClass;
	}

	private void resolveDashObjectAnnotation() {
		var annotation = this.dashClass.getDeclaredAnnotation(DashObject.class);
		if (annotation == null) {
			throw new RuntimeException("Registered Class " + this.dashClass.getSimpleName() + " does not have a @DashObject annotation.");
		}
		//noinspection unchecked
		this.targetClass = (Class<R>) annotation.value();
	}


	public int getDashObjectId() {
		return dashObjectId;
	}

	@Override
	public String toString() {
		return "DashObjectClass{" +
				"dashClass=" + dashClass +
				", targetClass=" + targetClass +
				", dashObjectId=" + dashObjectId +
				'}';
	}
}
