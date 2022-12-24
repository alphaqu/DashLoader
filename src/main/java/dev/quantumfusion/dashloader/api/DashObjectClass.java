package dev.quantumfusion.dashloader.api;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.api.DashDependencies;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.registry.factory.InheritanceHandling;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


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
	@Nullable
	private Class<?> category;
	int dashObjectId;
	@Deprecated
	@Nullable
	private Class<? extends Dashable<?>> dashTag;
	@Nullable
	private List<Class<?>> dependencies;

	public DashObjectClass(Class<D> dashClass) {
		this.dashClass = dashClass;
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

	@NotNull
	public Class<?> getCategory() {
		if (this.category == null) {
			resolveDashObjectAnnotation();
		}
		return this.category;
	}

	private void resolveDashObjectAnnotation() {
		var annotation = this.dashClass.getDeclaredAnnotation(DashObject.class);
		if (annotation == null) {
			throw new RuntimeException("Registered Class " + this.dashClass.getSimpleName() + " does not have a @DashObject annotation.");
		}
		//noinspection unchecked
		this.targetClass = (Class<R>) annotation.value();

		Class<?> category = annotation.category();
		if (category == Void.class)  {
			category = this.targetClass;
		}
		this.category = category;
	}

	// lazy
	@SuppressWarnings({"rawtypes", "RedundantSuppression", "RedundantCast"})
	@NotNull
	public Class<? extends Dashable<?>> getTag() {
		if (this.dashTag == null) {
			Class<? extends Dashable<?>> dashInterface = null;
			for (Class<?> anInterface : this.dashClass.getInterfaces()) {
				if (Dashable.class.isAssignableFrom(anInterface)) {
					//noinspection unchecked
					dashInterface = (Class<? extends Dashable<?>>) anInterface;
					break;
				}
			}

			if (dashInterface == null) {
				throw new RuntimeException(this.dashClass.getSimpleName() + " does not have an interface that inherits Dashable");
			}


			this.dashTag = (dashInterface == ((Class<? extends Dashable>) Dashable.class) ? this.dashClass : dashInterface);
		}
		return this.dashTag;
	}

	// lazy
	@NotNull
	public List<Class<?>> getDependencies() {
		if (this.dependencies == null) {
			Class<?>[] dependencies;
			var dependenciesAnnotation = this.dashClass.getDeclaredAnnotation(DashDependencies.class);
			if (dependenciesAnnotation == null) {
				dependencies = new Class[0];
			} else {
				dependencies = dependenciesAnnotation.value();
			}

			for (Class<?> dependency : dependencies) {
				if (dependency.getDeclaredAnnotation(DashObject.class) == null) {
					throw new RuntimeException(
							"Registered Class " + this.dashClass.getSimpleName() + " Dependency \"" + dependency.getSimpleName() +
									"\" does not have a @DashObject annotation and therefore is not a DashObject");
				}
			}
			this.dependencies = List.of(dependencies);
		}
		return this.dependencies;
	}

	public int getDashObjectId() {
		return dashObjectId;
	}

	@Override
	public String toString() {
		return "DashObjectClass{" +
				"dashClass=" + dashClass +
				", targetClass=" + targetClass +
				", category=" + category +
				", dashObjectId=" + dashObjectId +
				", dashTag=" + dashTag +
				", dependencies=" + dependencies +
				'}';
	}
}
